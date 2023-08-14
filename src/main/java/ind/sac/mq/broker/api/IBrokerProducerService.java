package ind.sac.mq.broker.api;

import ind.sac.mq.broker.support.ServiceEntry;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import io.netty.channel.Channel;

public interface IBrokerProducerService {

    MQCommonResponse register(final ServiceEntry serviceEntry, Channel channel);

    MQCommonResponse unRegister(final ServiceEntry serviceEntry, Channel channel);

    /**
     * Get info of service address.
     *
     * @param channelId String of ID of netty channel
     * @return Message common queue response including specific info
     */
    ServiceEntry getServiceEntry(final String channelId);

    void checkChannelValid(final String channelId);
}
