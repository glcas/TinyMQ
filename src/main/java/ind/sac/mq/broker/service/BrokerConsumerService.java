package ind.sac.mq.broker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.model.bo.GroupNameChannel;
import ind.sac.mq.broker.model.bo.ServiceEntry;
import ind.sac.mq.broker.model.bo.SubscribedConsumer;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.broker.model.po.MessagePO;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.request.HeartbeatRequest;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.consumer.dto.request.ConsumerSubscribeRequest;
import ind.sac.mq.consumer.dto.response.ConsumerPullResponse;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;

// Java传参是地址值传递，因此final修饰参数仅有提示作用
public interface BrokerConsumerService {

    /**
     * Register consume service, grouped by {@link ServiceEntry#getGroupName()}
     * contains all clients subscribed the serviceId
     */
    CommonResponse register(RegisterRequest registerRequest, Channel channel);

    CommonResponse unregister(RegisterRequest registerRequest, Channel channel) throws JsonProcessingException;

    CommonResponse subscribe(ConsumerSubscribeRequest consumerSubscribeRequest, Channel channel);

    CommonResponse unsubscribe(ConsumerSubscribeRequest consumerSubscribeRequest, Channel channel);

    /**
     * Get list containing consumer's group & client channel.
     */
    List<GroupNameChannel> listSubscribedConsumers(Message message);

    CommonResponse heartbeat(HeartbeatRequest mqHeartbeatRequest, Channel channel) throws JsonProcessingException;

    void notifyPendingConsumers(MessagePO messagePO);

    ConsumerPullResponse longPolling(String requestJSON, Map<String, List<MessagePO>> messageByTopicMap, Channel channel) throws JsonProcessingException;

    void checkChannelValid(String channelId);

    Map<String, SubscribedConsumer> getSubscribedConsumerMap();
}
