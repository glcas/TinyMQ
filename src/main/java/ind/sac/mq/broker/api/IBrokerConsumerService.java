package ind.sac.mq.broker.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.dto.consumer.ConsumerSubscribeRequest;
import ind.sac.mq.broker.support.GroupNameChannel;
import ind.sac.mq.broker.support.ServiceEntry;
import ind.sac.mq.common.dto.request.MQHeartbeatRequest;
import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import io.netty.channel.Channel;

import java.util.List;

public interface IBrokerConsumerService {

    /**
     * Register consume service, grouped by {@link ServiceEntry#getGroupName()}
     * contains all clients subscribed the serviceId
     */
    MQCommonResponse register(final ServiceEntry serviceEntry, Channel channel);

    MQCommonResponse unregister(final ServiceEntry serviceEntry, Channel channel) throws JsonProcessingException;

    MQCommonResponse subscribe(final ConsumerSubscribeRequest consumerSubscribeRequest, final Channel clientChannel);

    MQCommonResponse unsubscribe(final ConsumerSubscribeRequest consumerSubscribeRequest, final Channel clientChannel);

    /**
     * Get list containing consumer's group & client channel.
     */
    List<GroupNameChannel> getPushedSubscribeList(MQMessage mqMessage);

    MQCommonResponse heartbeat(final MQHeartbeatRequest mqHeartbeatRequest, Channel channel) throws JsonProcessingException;

    void checkChannelValid(final String channelId);
}
