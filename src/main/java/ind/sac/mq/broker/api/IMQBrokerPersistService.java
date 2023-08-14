package ind.sac.mq.broker.api;

import ind.sac.mq.broker.dto.persist.MQPersistPutMsg;
import ind.sac.mq.common.dto.request.MQConsumerPullRequest;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.dto.response.MQConsumerPullResponse;
import io.netty.channel.Channel;

public interface IMQBrokerPersistService {

    MQCommonResponse put(final MQPersistPutMsg mqMsg);

    MQCommonResponse updateStatus(long traceId, String status);

    MQConsumerPullResponse pull(final MQConsumerPullRequest pullRequest, final Channel channel);
}
