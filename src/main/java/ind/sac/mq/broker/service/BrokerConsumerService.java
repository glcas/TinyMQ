package ind.sac.mq.broker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.model.bo.GroupNameChannel;
import ind.sac.mq.broker.model.bo.ServiceEntry;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.request.HeartbeatRequest;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.consumer.dto.request.ConsumerSubscribeRequest;
import io.netty.channel.Channel;

import java.util.List;

// Java传参是地址值传递，因此final修饰参数仅有提示作用
public interface BrokerConsumerService {

    /**
     * Register consume service, grouped by {@link ServiceEntry#getGroupName()}
     * contains all clients subscribed the serviceId
     */
    CommonResponse register(final RegisterRequest registerRequest, final Channel channel);

    CommonResponse unregister(final RegisterRequest registerRequest, final Channel channel) throws JsonProcessingException;

    CommonResponse subscribe(final ConsumerSubscribeRequest consumerSubscribeRequest, final Channel channel);

    CommonResponse unsubscribe(final ConsumerSubscribeRequest consumerSubscribeRequest, final Channel channel);

    /**
     * Get list containing consumer's group & client channel.
     */
    List<GroupNameChannel> listSubscribedConsumers(Message message);

    CommonResponse heartbeat(final HeartbeatRequest mqHeartbeatRequest, Channel channel) throws JsonProcessingException;

    void checkChannelValid(final String channelId);
}
