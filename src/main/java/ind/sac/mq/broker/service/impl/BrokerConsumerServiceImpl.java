package ind.sac.mq.broker.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.constant.BrokerResponseCode;
import ind.sac.mq.broker.model.bo.GroupNameChannel;
import ind.sac.mq.broker.model.bo.PendingConsumer;
import ind.sac.mq.broker.model.bo.ServiceEntry;
import ind.sac.mq.broker.model.bo.SubscribedConsumer;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.broker.model.po.MessagePO;
import ind.sac.mq.broker.service.BrokerConsumerService;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.request.HeartbeatRequest;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.CommonResponseCode;
import ind.sac.mq.common.util.JsonUtil;
import ind.sac.mq.common.util.RandomUtil;
import ind.sac.mq.common.util.RegexUtil;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import ind.sac.mq.consumer.constant.ConsumerResponseCode;
import ind.sac.mq.consumer.dto.request.ConsumerPullRequest;
import ind.sac.mq.consumer.dto.request.ConsumerSubscribeRequest;
import ind.sac.mq.consumer.dto.response.ConsumerPullResponse;
import ind.sac.mq.consumer.record.SubscribeInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BrokerConsumerServiceImpl implements BrokerConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(BrokerConsumerServiceImpl.class);

    /**
     * Scheduled remove consumer whose last access time exceeds 2 minutes.
     */
    private static final ScheduledExecutorService heartbeatScheduledService = Executors.newSingleThreadScheduledExecutor();

    /**
     * key: channel ID
     * value: ServiceEntry
     */
    private final Map<String, ServiceEntry> registerMap = new ConcurrentHashMap<>();

    /**
     * Subscriber map by topic
     * key: topic name
     * value: specific topic's subscriber(consumer) set
     */
    private final Map<String, Set<SubscribedConsumer>> subscriberByTopicMap = new ConcurrentHashMap<>();

    /**
     * Subscriber map by channel id
     * key: channel id
     * value: subscriber(consumer)
     */
    private final Map<String, SubscribedConsumer> subscribedConsumerMap = new ConcurrentHashMap<>();

    // kv: channelID - record PendingConsumer
    private final Map<String, PendingConsumer> pendingConsumers = new ConcurrentHashMap<>();

    private final long longPollingTimeout = 30000;

    @Override
    public Map<String, SubscribedConsumer> getSubscribedConsumerMap() {
        return subscribedConsumerMap;
    }

    public BrokerConsumerServiceImpl() {
        // scan scheduled 2 minutes
        // 通过心跳机制维护消费者与broker之间的长连接，移除超过2分钟没有发来心跳的宕机消费者
        final long limitMilliseconds = 2 * 60 * 1000;
        heartbeatScheduledService.scheduleAtFixedRate(() -> {
            for (ServiceEntry serviceEntry : registerMap.values()) {
                long lastAccessTime = serviceEntry.getLastAccessTime();
                if (System.currentTimeMillis() - lastAccessTime > limitMilliseconds) {
                    try {
                        removeChannel(serviceEntry.getChannel());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 2 * 60, 2 * 60, TimeUnit.SECONDS);
    }

    @Override
    public CommonResponse register(RegisterRequest registerRequest, Channel channel) {
        final String channelId = channel.id().asLongText();
        ServiceEntry serviceEntry = new ServiceEntry(registerRequest.getGroupName(), channel, registerRequest.getWeight());
        serviceEntry.setLastAccessTime(System.currentTimeMillis());
        registerMap.put(channelId, serviceEntry);

        // construct successful response and return
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public CommonResponse unregister(RegisterRequest registerRequest, Channel channel) throws JsonProcessingException {
        removeChannel(channel);
        // construct successful response and return
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public CommonResponse subscribe(ConsumerSubscribeRequest consumerSubRequest, Channel channel) {
        final String topicName = consumerSubRequest.getTopicName();
        SubscribedConsumer subscribedConsumer = new SubscribedConsumer(consumerSubRequest.getGroupName(), topicName, consumerSubRequest.getTagRegex(), channel.id().asLongText());

        // put subscribedConsumer to topicName's set
        Set<SubscribedConsumer> topicSet = this.subscriberByTopicMap.get(topicName);
        if (topicSet == null) {
            topicSet = ConcurrentHashMap.newKeySet();
        }
        topicSet.add(subscribedConsumer);
        this.subscriberByTopicMap.put(topicName, topicSet);

        // put subscribedConsumer to channelID map
        subscribedConsumerMap.put(channel.id().asLongText(), subscribedConsumer);

        // construct successful response and return
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public CommonResponse unsubscribe(ConsumerSubscribeRequest consumerSubRequest, Channel channel) {
        final String topicName = consumerSubRequest.getTopicName();
        SubscribedConsumer subscribedConsumer = new SubscribedConsumer(consumerSubRequest.getGroupName(), topicName, consumerSubRequest.getTagRegex(), channel.id().asLongText());

        Set<SubscribedConsumer> topicSet = this.subscriberByTopicMap.get(topicName);
        if (!topicSet.isEmpty()) {
            topicSet.remove(subscribedConsumer);
        }

        this.subscribedConsumerMap.remove(channel.id().asLongText());

        // construct successful response and return
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    /**
     * 从请求话题(Topic)集合找到Tag能正则匹配请求的若干实体，将他们按照groupName分组后，每组选1个来响应<br/>
     * 在RocketMQ中，Topic是消息的一级分类，Tag是消息的二级分类；以groupName的分组可视为用于集群灾备
     *
     * @param message 请求消息，主要使用订阅的话题、标签
     * @return 处理对应请求的业务已注册的通道列表，即消息过来，找到匹配的订阅消费者
     */
    @Override
    public List<GroupNameChannel> listSubscribedConsumers(Message message) {
        Set<SubscribedConsumer> consumerSet = this.subscriberByTopicMap.get(message.getTopic());
        // 当subscribeMap不包含指定Topic的键时会返回null，若不判null会导致NPE
        if (consumerSet == null || consumerSet.isEmpty()) {
            return Collections.emptyList();
        }

        // 用SubscribedConsumer里的正则匹配请求中的标签列表（生产者生成的消息附带有若干tags，每个消费者可接受一定范围的标签，以正则表示）
        // 只要消息的若干tags匹配上了一个就说明消费者需要该消息，将消费者按其groupName分组添加进map<groupName, list_of_consumers>中
        Map<String, List<SubscribedConsumer>> subscribedConsumerByGroupNameMap = new HashMap<>();
        for (SubscribedConsumer subscribedConsumer : consumerSet) {
            if (RegexUtil.hasMatch(subscribedConsumer.getTagRegex(), message.getTags())) {
                String groupName = subscribedConsumer.getGroupName();
                List<SubscribedConsumer> sameGroupNameSubscribedConsumers = subscribedConsumerByGroupNameMap.get(groupName);
                if (sameGroupNameSubscribedConsumers == null) {
                    sameGroupNameSubscribedConsumers = new ArrayList<>();
                }
                sameGroupNameSubscribedConsumers.add(subscribedConsumer);
                subscribedConsumerByGroupNameMap.put(groupName, sameGroupNameSubscribedConsumers);
            }
        }

        // SubscribedConsumer按group name分组后，从每一组随机返回一个即可。(最好应精心设计为根据sharding key进行选择)
        List<GroupNameChannel> groupNameChannelList = new ArrayList<>();
        for (Map.Entry<String, List<SubscribedConsumer>> entry : subscribedConsumerByGroupNameMap.entrySet()) {
            List<SubscribedConsumer> subscribedConsumers = entry.getValue();
            SubscribedConsumer subscribedConsumer = RandomUtil.loadBalance(subscribedConsumers, message.getShardingKey());
            if (subscribedConsumer == null) {
                continue;
            }
            final String channelId = subscribedConsumer.getChannelId();
            ServiceEntry serviceEntry = registerMap.get(channelId);
            if (serviceEntry == null) {
                logger.warn("Channel ID {} isn't registered.", channelId);
                continue;
            }
            final String groupName = entry.getKey();
            GroupNameChannel groupNameChannel = GroupNameChannel.of(groupName, serviceEntry.getChannel());
            groupNameChannelList.add(groupNameChannel);
        }
        return groupNameChannelList;
    }

    @Override
    public CommonResponse heartbeat(HeartbeatRequest heartbeatRequest, Channel channel) throws JsonProcessingException {
        final String channelId = channel.id().asLongText();
        logger.debug("[Heartbeat - {}] Received from consumer, channel ID: {}", JsonUtil.writeAsJsonString(heartbeatRequest), channelId);
        ServiceEntry serviceEntry = Objects.requireNonNull(registerMap.get(channelId));
        serviceEntry.setLastAccessTime(System.currentTimeMillis());
        registerMap.put(channelId, serviceEntry);  // 不是putIfAbsent，可更新Map
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public void notifyPendingConsumers(MessagePO messagePO) {
        Message message = messagePO.getMessage();
        // 此主题+标签消息目前有消费者订阅，并且满足条件的消费者已在broker挂起，响应消费者的pull请求
        List<GroupNameChannel> channelList = listSubscribedConsumers(message);
        for (GroupNameChannel groupNameChannel : channelList) {
            String channelID = groupNameChannel.getChannel().id().asLongText();
            PendingConsumer pendingConsumer = this.pendingConsumers.get(channelID);
            if (pendingConsumer == null) continue;
            pendingConsumer.messages().add(message);
            synchronized (pendingConsumer.channelThread()) {
                pendingConsumer.channelThread().notify();
            }
            messagePO.setConsumeStatus(ConsumeStatus.PROCESSING);
        }
    }

    /**
     * 处理消费者发来的长轮询拉取消息请求 <br/>
     * 1. 遍历一遍broker里持久化存储的消息，若匹配上了就更新其状态，并直接返回给消费者 <br/>
     * 2. 否则，将这个group-name channel挂起，等待生产者的消息来了作为响应返回
     */
    @Override
    public ConsumerPullResponse longPolling(String requestJSON, Map<String, List<MessagePO>> messageByTopicMap, Channel channel) throws JsonProcessingException {
        ConsumerPullRequest pullRequest = JsonUtil.parseJson(requestJSON, ConsumerPullRequest.class);
        List<Message> neededMessages = new ArrayList<>();
        for (SubscribeInfo subscribeInfo : pullRequest.getSubscribeInfos()) {
            List<MessagePO> messagePOS = Optional.ofNullable(messageByTopicMap.get(subscribeInfo.topicName())).orElse(Collections.emptyList());
            for (MessagePO messagePO : messagePOS) {
                // 只拉取未被消费过的消息
                if (messagePO.getConsumeStatus() == ConsumeStatus.IDLE && RegexUtil.hasMatch(subscribeInfo.tagRegex(), messagePO.getMessage().getTags())) {
                    messagePO.setConsumeStatus(ConsumeStatus.PROCESSING);  // 独占加锁，实现点对点模式，存量消息只能被一个消费者消费
                    neededMessages.add(messagePO.getMessage());
                    if (neededMessages.size() >= pullRequest.getPullSize()) break;
                }
            }
            if (neededMessages.size() >= pullRequest.getPullSize()) break;
        }
        if (neededMessages.isEmpty()) {
            // hang on consumer
            String channelID = channel.id().asLongText();
            Thread currentThread = Thread.currentThread();
            this.pendingConsumers.put(channelID, new PendingConsumer(currentThread, new ArrayList<>()));
            synchronized (currentThread) {
                try {
                    currentThread.wait(longPollingTimeout);
                } catch (InterruptedException e) {
                    currentThread.interrupt();
                }
            }
            // producer发来了所需消息，或等待超时，被唤醒
            neededMessages = this.pendingConsumers.get(channelID).messages();
//            logger.info(neededMessages.toString());
            this.pendingConsumers.remove(channelID);
        }
        return new ConsumerPullResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage(), neededMessages);
    }

    @Override
    public void checkChannelValid(String channelId) {
        if (!registerMap.containsKey(channelId)) {
            logger.error("Channel ID: {} isn't registered.", channelId);
            throw new MQException(BrokerResponseCode.CONSUMER_REGISTER_CHANNEL_NOT_VALID);
        }
    }

    private void removeChannel(final Channel channel) throws JsonProcessingException {
        // 移除本地记录
        String channelId = channel.id().asLongText();
        ServiceEntry removedServiceEntry = registerMap.remove(channelId);
        logger.info("Register info removed - channel id: {}, serviceEntry: {}", channelId, JsonUtil.writeAsJsonString(removedServiceEntry));

        // 关闭连接通道
        channel.closeFuture().addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                throw new MQException(future.cause(), ConsumerResponseCode.CONSUMER_SHUTDOWN_ERROR);
            }
        });
        channel.close();
    }
}
