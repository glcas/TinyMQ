package ind.sac.mq.broker.service;

import ind.sac.mq.broker.model.po.MessagePO;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import ind.sac.mq.consumer.dto.request.ConsumerPullRequest;
import ind.sac.mq.consumer.dto.response.ConsumerPullResponse;
import io.netty.channel.Channel;

public interface BrokerPersistenceService {

    CommonResponse save(final MessagePO mqMsg);

    CommonResponse updateStatus(long traceId, ConsumeStatus status);

    ConsumerPullResponse pull(final ConsumerPullRequest pullRequest, final Channel channel);
}
