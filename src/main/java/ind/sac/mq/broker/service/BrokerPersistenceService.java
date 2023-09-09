package ind.sac.mq.broker.service;

import ind.sac.mq.broker.model.po.MessagePO;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.consumer.constant.ConsumeStatus;

import java.util.List;
import java.util.Map;

public interface BrokerPersistenceService {

    CommonResponse save(final MessagePO mqMsg);

    CommonResponse updateStatus(long traceId, ConsumeStatus status);

    Map<String, List<MessagePO>> getMessageByTopicMap();
}
