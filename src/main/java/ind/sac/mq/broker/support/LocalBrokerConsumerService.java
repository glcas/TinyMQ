package ind.sac.mq.broker.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.api.IBrokerConsumerService;
import ind.sac.mq.broker.constant.MQBrokerResponseCode;
import ind.sac.mq.broker.dto.consumer.ConsumerSubscribeRequest;
import ind.sac.mq.broker.dto.consumer.SubscribedConsumer;
import ind.sac.mq.broker.utils.ChannelUtils;
import ind.sac.mq.common.dto.request.MQHeartbeatRequest;
import ind.sac.mq.common.dto.request.MQRequestMessage;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.MQCommonResponseCode;
import ind.sac.mq.common.utils.JsonUtil;
import ind.sac.mq.common.utils.RandomUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalBrokerConsumerService implements IBrokerConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(LocalBrokerConsumerService.class);
    /**
     * Scheduled remove BrokerServiceEntryChannel whose last access time exceeds 2 minutes.
     */
    private static final ScheduledExecutorService heartbeatScheduledService = Executors.newSingleThreadScheduledExecutor();
    /**
     * key: channel ID
     * value: BrokerServiceEntryChannel
     */
    private final Map<String, BrokerServiceEntryChannel> registerMap = new ConcurrentHashMap<>();
    /**
     * Subscribe map in push-policy
     * key: topic name
     * value: specific topic's subscribe set
     */
    private final Map<String, Set<SubscribedConsumer>> pushedSubscribeMap = new ConcurrentHashMap<>();
    /**
     * key: channel ID
     * value: BrokerServiceEntryChannel
     */
    private final Map<String, BrokerServiceEntryChannel> heartbeatMap = new ConcurrentHashMap<>();

    public LocalBrokerConsumerService() {
        // scan scheduled 2 minutes
        // 通过心跳机制维护消费者与broker之间的长连接，移除超过2分钟没有所需消息发来的挂起消费者
        final long limitMilliseconds = 2 * 60 * 1000;
        heartbeatScheduledService.scheduleAtFixedRate(() -> {
            for (Map.Entry<String, BrokerServiceEntryChannel> entry : heartbeatMap.entrySet()) {
                String key = entry.getKey();
                long lastAccessTime = entry.getValue().getLastAccessTime();
                if (System.currentTimeMillis() - lastAccessTime > limitMilliseconds) {
                    try {
                        removeByChannelId(key);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 2 * 60, 2 * 60, TimeUnit.SECONDS);
    }

    @Override
    public MQCommonResponse register(ServiceEntry serviceEntry, Channel channel) {
        final String channelId = channel.id().asLongText();
        BrokerServiceEntryChannel entryChannel = ChannelUtils.buildEntryChannel(serviceEntry, channel);
        registerMap.put(channelId, entryChannel);
        entryChannel.setLastAccessTime(System.currentTimeMillis());
        heartbeatMap.put(channelId, entryChannel);

        // construct successful response and return
        return new MQCommonResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription());
    }

    @Override
    public MQCommonResponse unregister(ServiceEntry serviceEntry, Channel channel) throws JsonProcessingException {
        final String channelId = channel.id().asLongText();
        removeByChannelId(channelId);
        // construct successful response and return
        return new MQCommonResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription());
    }

    @Override
    public MQCommonResponse subscribe(ConsumerSubscribeRequest consumerSubRequest, Channel clientChannel) {
        Map<String, Set<SubscribedConsumer>> subscribeMap = getSubscribeMapByConsumerType(consumerSubRequest.getConsumerType());
        final String topicName = consumerSubRequest.getTopicName();
        SubscribedConsumer subscribeBO = new SubscribedConsumer(consumerSubRequest.getGroupName(), topicName, consumerSubRequest.getTagRegex(), clientChannel.id().asLongText());

        // put subscribeBO to topicName's set
        Set<SubscribedConsumer> topicSet = subscribeMap.get(topicName);
        if (topicSet == null) {
            topicSet = new HashSet<>();
        }
        topicSet.add(subscribeBO);
        subscribeMap.put(topicName, topicSet);

        // construct successful response and return
        return new MQCommonResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription());
    }

    @Override
    public MQCommonResponse unsubscribe(ConsumerSubscribeRequest consumerSubRequest, Channel clientChannel) {
        Map<String, Set<SubscribedConsumer>> subscribeMap = getSubscribeMapByConsumerType(consumerSubRequest.getConsumerType());
        final String topicName = consumerSubRequest.getTopicName();
        SubscribedConsumer subscribeBO = new SubscribedConsumer(consumerSubRequest.getGroupName(), topicName, consumerSubRequest.getTagRegex(), clientChannel.id().asLongText());

        Set<SubscribedConsumer> topicSet = subscribeMap.get(topicName);
        if (!topicSet.isEmpty()) {
            topicSet.remove(subscribeBO);
        }

        // construct successful response and return
        return new MQCommonResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription());
    }

    /**
     * 从请求话题(Topic)集合找到Tag能正则匹配请求的若干实体，将他们按照groupName分组后，每组选1个来响应<p/>
     * 在RocketMQ中，Topic是消息的一级分类，Tag是消息的二级分类；以groupName的分组可视为用于集群灾备
     *
     * @param mqRequestMessage 请求消息，主要使用订阅的话题、标签
     * @return 处理对应请求的业务已注册的通道列表，即消息过来，找到匹配的订阅消费者
     */
    @Override
    public List<GroupNameChannel> getPushedSubscribeList(MQRequestMessage mqRequestMessage) {
        Set<SubscribedConsumer> consumerSet = pushedSubscribeMap.get(mqRequestMessage.getTopic());
        if (consumerSet.isEmpty()) {
            return Collections.emptyList();
        }

        // 用SubscribedConsumer里的正则匹配请求中的标签列表（生产者生成的消息附带有若干tags，每个消费者可接受一定范围的标签，以正则表示）
        // 只要消息的若干tags匹配上了一个就说明消费者需要该消息，将消费者按其groupName分组添加进map<groupName, list_of_consumers>中
        final List<String> tagNameList = mqRequestMessage.getTags();
        Map<String, List<SubscribedConsumer>> groupMap = new HashMap<>();
        for (SubscribedConsumer consumer : consumerSet) {
            boolean hasMatch = false;
            Pattern pattern = Pattern.compile(consumer.getTagRegex());
            for (String tagName : tagNameList) {
                Matcher matcher = pattern.matcher(tagName);
                if (matcher.find()) {
                    hasMatch = true;
                    break;
                }
            }
            if (hasMatch) {
                String groupName = consumer.getGroupName();
                List<SubscribedConsumer> groupList = groupMap.get(groupName);
                if (groupList == null) {
                    groupList = new ArrayList<>();
                }
                groupList.add(consumer);
                groupMap.put(groupName, groupList);
            }
        }

        // SubscribedConsumer按group name分组后，从每一组随机返回一个即可。
        // 最好应精心设计为根据sharding key进行选择
        final String shardingKey = mqRequestMessage.getShardingKey();
        List<GroupNameChannel> groupNameChannelList = new ArrayList<>();
        for (Map.Entry<String, List<SubscribedConsumer>> entry : groupMap.entrySet()) {
            List<SubscribedConsumer> consumerSubBOList = entry.getValue();
            SubscribedConsumer consumer = RandomUtil.loadBalance(consumerSubBOList, shardingKey);
            if (consumer == null) {
                continue;
            }
            final String channelId = consumer.getChannelId();
            BrokerServiceEntryChannel entryChannel = registerMap.get(channelId);
            if (entryChannel == null) {
                logger.warn("Channel ID {} isn't registered.", channelId);
                continue;
            }
            final String groupName = entry.getKey();
            GroupNameChannel groupNameChannel = GroupNameChannel.of(groupName, entryChannel.getChannel());
            groupNameChannelList.add(groupNameChannel);
        }
        return groupNameChannelList;
    }

    @Override
    public void heartbeat(MQHeartbeatRequest mqHeartbeatRequest, Channel channel) throws JsonProcessingException {
        final String channelId = channel.id().asLongText();
        logger.info("[Heartbeat - {}] Received from consumer, channel ID: {}", JsonUtil.writeAsJsonString(mqHeartbeatRequest), channelId);
        ServiceEntry serviceEntry = new ServiceEntry(mqHeartbeatRequest.getAddress(), mqHeartbeatRequest.getPort());
        BrokerServiceEntryChannel entryChannel = ChannelUtils.buildEntryChannel(serviceEntry, channel);
        entryChannel.setLastAccessTime(mqHeartbeatRequest.getTime());
        heartbeatMap.put(channelId, entryChannel);
    }

    @Override
    public void checkChannelValid(String channelId) {
        if (!registerMap.containsKey(channelId)) {
            logger.error("Channel ID: {} isn't registered.", channelId);
            throw new MQException(MQBrokerResponseCode.CONSUMER_REGISTER_CHANNEL_NOT_VALID);
        }
    }

    private void removeByChannelId(final String channelId) throws JsonProcessingException {
        BrokerServiceEntryChannel channelRegister = registerMap.remove(channelId);
        logger.info("Register info removed - id: {}, channel: {}", channelId, JsonUtil.writeAsJsonString(channelRegister));
        BrokerServiceEntryChannel channelHeartbeat = heartbeatMap.remove(channelId);
        logger.info("Heartbeat info removed - id: {}, channel: {}", channelId, JsonUtil.writeAsJsonString(channelHeartbeat));
    }

    private Map<String, Set<SubscribedConsumer>> getSubscribeMapByConsumerType(String consumerType) {
        // TODO: expand consumer type
        return pushedSubscribeMap;
    }
}
