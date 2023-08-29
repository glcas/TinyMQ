package ind.sac.mq.broker.service.impl;

import ind.sac.mq.broker.model.po.MessagePO;
import ind.sac.mq.broker.service.BrokerPersistenceService;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.common.response.CommonResponseCode;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import ind.sac.mq.consumer.dto.request.ConsumerPullRequest;
import ind.sac.mq.consumer.dto.response.ConsumerPullResponse;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrokerPersistenceServiceImpl implements BrokerPersistenceService {

    /**
     * key: topic, value: MessagePO
     */
    private final Map<String, List<MessagePO>> messageByTopicMap = new ConcurrentHashMap<>();

    /**
     * key: traceId, value: MessagePO. 用于updateStatus加速，以空间换时间
     */
    private final Map<Long, MessagePO> messageByTraceIDMap = new ConcurrentHashMap<>();

    @Override
    public synchronized CommonResponse save(MessagePO messagePO) {
        Message message = messagePO.getMessage();
        final String topic = message.getTopic();
        List<MessagePO> messagePOList = messageByTopicMap.get(topic);
        if (messagePOList == null) {
            messagePOList = new ArrayList<>();
        }
        messagePOList.add(messagePO);
        messageByTopicMap.put(topic, messagePOList);

        messageByTraceIDMap.put(messagePO.getMessage().getTraceId(), messagePO);

        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public CommonResponse updateStatus(long traceId, ConsumeStatus status) {
        MessagePO msg = messageByTraceIDMap.get(traceId);
        msg.setConsumeStatus(status);
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public ConsumerPullResponse pull(ConsumerPullRequest pullRequest, Channel channel) {
        return null;
    }
}
