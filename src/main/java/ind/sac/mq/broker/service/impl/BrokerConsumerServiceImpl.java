package ind.sac.mq.broker.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.constant.BrokerResponseCode;
import ind.sac.mq.broker.model.bo.GroupNameChannel;
import ind.sac.mq.broker.model.bo.ServiceEntry;
import ind.sac.mq.broker.model.bo.SubscribedConsumer;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.broker.service.BrokerConsumerService;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.request.HeartbeatRequest;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.CommonResponseCode;
import ind.sac.mq.common.util.JsonUtil;
import ind.sac.mq.common.util.RandomUtil;
import ind.sac.mq.consumer.constant.ConsumerResponseCode;
import ind.sac.mq.consumer.dto.request.ConsumerSubscribeRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Subscribe map
     * key: topic name
     * value: specific topic's subscriber(consumer) set
     */
    private final Map<String, Set<SubscribedConsumer>> subscribeMap = new ConcurrentHashMap<>();

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
        Map<String, Set<SubscribedConsumer>> subscribeMap = getSubscribeMapByConsumerType(consumerSubRequest.getConsumerType());
        final String topicName = consumerSubRequest.getTopicName();
        SubscribedConsumer subscribedConsumer = new SubscribedConsumer(consumerSubRequest.getGroupName(), topicName, consumerSubRequest.getTagRegex(), channel.id().asLongText());

        // put subscribedConsumer to topicName's set
        Set<SubscribedConsumer> topicSet = subscribeMap.get(topicName);
        if (topicSet == null) {
            topicSet = new HashSet<>();
        }
        topicSet.add(subscribedConsumer);
        subscribeMap.put(topicName, topicSet);

        // construct successful response and return
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public CommonResponse unsubscribe(ConsumerSubscribeRequest consumerSubRequest, Channel channel) {
        Map<String, Set<SubscribedConsumer>> subscribeMap = getSubscribeMapByConsumerType(consumerSubRequest.getConsumerType());
        final String topicName = consumerSubRequest.getTopicName();
        SubscribedConsumer subscribedConsumer = new SubscribedConsumer(consumerSubRequest.getGroupName(), topicName, consumerSubRequest.getTagRegex(), channel.id().asLongText());

        Set<SubscribedConsumer> topicSet = subscribeMap.get(topicName);
        if (!topicSet.isEmpty()) {
            topicSet.remove(subscribedConsumer);
        }

        // construct successful response and return
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    /**
     * 从请求话题(Topic)集合找到Tag能正则匹配请求的若干实体，将他们按照groupName分组后，每组选1个来响应<p/>
     * 在RocketMQ中，Topic是消息的一级分类，Tag是消息的二级分类；以groupName的分组可视为用于集群灾备
     *
     * @param message 请求消息，主要使用订阅的话题、标签
     * @return 处理对应请求的业务已注册的通道列表，即消息过来，找到匹配的订阅消费者
     */
    @Override
    public List<GroupNameChannel> listSubscribedConsumers(Message message) {
        Set<SubscribedConsumer> consumerSet = this.subscribeMap.get(message.getTopic());
        // 当subscribeMap不包含指定Topic的键时会返回null，若不判null会导致NPE
        if (consumerSet == null || consumerSet.isEmpty()) {
            return Collections.emptyList();
        }

        // 用SubscribedConsumer里的正则匹配请求中的标签列表（生产者生成的消息附带有若干tags，每个消费者可接受一定范围的标签，以正则表示）
        // 只要消息的若干tags匹配上了一个就说明消费者需要该消息，将消费者按其groupName分组添加进map<groupName, list_of_consumers>中
        final List<String> tagNameList = message.getTags();
        Map<String, List<SubscribedConsumer>> subscribedConsumerByGroupNameMap = new HashMap<>();
        for (SubscribedConsumer subscribedConsumer : consumerSet) {
            boolean hasMatch = false;
            Pattern pattern = Pattern.compile(subscribedConsumer.getTagRegex());
            for (String tagName : tagNameList) {
                Matcher matcher = pattern.matcher(tagName);
                if (matcher.find()) {
                    hasMatch = true;
                    break;
                }
            }
            if (hasMatch) {
                String groupName = subscribedConsumer.getGroupName();
                List<SubscribedConsumer> sameGroupNameSubscribedConsumers = subscribedConsumerByGroupNameMap.get(groupName);
                if (sameGroupNameSubscribedConsumers == null) {
                    sameGroupNameSubscribedConsumers = new ArrayList<>();
                }
                sameGroupNameSubscribedConsumers.add(subscribedConsumer);
                subscribedConsumerByGroupNameMap.put(groupName, sameGroupNameSubscribedConsumers);
            }
        }

        // SubscribedConsumer按group name分组后，从每一组随机返回一个即可。
        // 最好应精心设计为根据sharding key进行选择
        final String shardingKey = message.getShardingKey();
        List<GroupNameChannel> groupNameChannelList = new ArrayList<>();
        for (Map.Entry<String, List<SubscribedConsumer>> entry : subscribedConsumerByGroupNameMap.entrySet()) {
            List<SubscribedConsumer> subscribedConsumers = entry.getValue();
            SubscribedConsumer subscribedConsumer = RandomUtil.loadBalance(subscribedConsumers, shardingKey);
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

    private Map<String, Set<SubscribedConsumer>> getSubscribeMapByConsumerType(String consumerType) {
        // TODO: expand consumer type
        return this.subscribeMap;
    }
}
