package ind.sac.mq.broker.support;

import ind.sac.mq.broker.api.IMQBrokerPersistService;
import ind.sac.mq.broker.dto.persist.MQPersistPutMsg;
import ind.sac.mq.common.dto.request.MQConsumerPullRequest;
import ind.sac.mq.common.dto.request.MQRequestMessage;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.dto.response.MQConsumerPullResponse;
import ind.sac.mq.common.response.MQCommonResponseCode;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalBrokerPersistService implements IMQBrokerPersistService {

    /**
     * key: topic, value: MQPersistPutMsg
     */
    private final Map<String, List<MQPersistPutMsg>> queueMap = new ConcurrentHashMap<>();

    /**
     * key: traceId, value: MQPersistPutMsg. 用于updateStatus加速，以空间换时间
     */
    private final Map<Long, MQPersistPutMsg> idMap = new ConcurrentHashMap<>();

    @Override
    public synchronized MQCommonResponse put(MQPersistPutMsg mqMsg) {
        MQRequestMessage requestMessage = mqMsg.getMqRequestMessage();
        final String topic = requestMessage.getTopic();
        List<MQPersistPutMsg> msgList = queueMap.get(topic);
        if (msgList == null) {
            msgList = new ArrayList<>();
        }
        msgList.add(mqMsg);
        queueMap.put(topic, msgList);

        idMap.put(mqMsg.getMqRequestMessage().getTraceId(), mqMsg);

        return new MQCommonResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription());
    }

    @Override
    public MQCommonResponse updateStatus(long traceId, String status) {
        MQPersistPutMsg msg = idMap.get(traceId);
        msg.setMsgStatus(status);
        return new MQCommonResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription());
    }

    @Override
    public MQConsumerPullResponse pull(MQConsumerPullRequest pullRequest, Channel channel) {
        return null;
    }
}
