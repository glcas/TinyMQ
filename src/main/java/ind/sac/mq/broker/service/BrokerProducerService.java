package ind.sac.mq.broker.service;

import ind.sac.mq.broker.model.bo.ServiceEntry;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.common.dto.response.CommonResponse;
import io.netty.channel.Channel;

public interface BrokerProducerService {

    CommonResponse register(final RegisterRequest registerRequest, Channel channel);

    CommonResponse unregister(final RegisterRequest registerRequest, Channel channel);

    /**
     * Get info of service address.
     *
     * @param channelId String of ID of netty channel
     * @return Message common queue response including specific info
     */
    ServiceEntry getServiceEntry(final String channelId);

    void checkChannelValid(final String channelId);
}
