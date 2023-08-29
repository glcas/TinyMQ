package ind.sac.mq.broker.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.model.bo.BrokerPushContext;
import ind.sac.mq.broker.model.bo.GroupNameChannel;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.broker.model.po.MessagePO;
import ind.sac.mq.broker.service.BrokerConsumerService;
import ind.sac.mq.broker.service.BrokerPersistenceService;
import ind.sac.mq.broker.service.BrokerProducerService;
import ind.sac.mq.broker.service.BrokerPushService;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.request.HeartbeatRequest;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.common.response.CommonResponseCode;
import ind.sac.mq.common.rpc.RPCMessage;
import ind.sac.mq.common.service.invoke.InvokeService;
import ind.sac.mq.common.util.JsonUtil;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import ind.sac.mq.consumer.dto.request.ConsumerPullRequest;
import ind.sac.mq.consumer.dto.request.ConsumerSubscribeRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

public class BrokerHandler extends SimpleChannelInboundHandler {

    private final InvokeService invokeService;

    private final BrokerConsumerService consumerService;

    private final BrokerProducerService producerService;

    private final BrokerPersistenceService persistService;

    private final BrokerPushService pushService;

    private final long responseTimeout;

    public BrokerHandler(InvokeService invokeService, BrokerConsumerService consumerService, BrokerProducerService producerService, BrokerPersistenceService persistService, BrokerPushService pushService, long responseTimeout) {
        this.invokeService = invokeService;
        this.consumerService = consumerService;
        this.producerService = producerService;
        this.persistService = persistService;
        this.pushService = pushService;
        this.responseTimeout = responseTimeout;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
        ByteBuf byteBuf = (ByteBuf) o;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        RPCMessage rpcMessage = JsonUtil.parseJson(bytes, RPCMessage.class);
        if (rpcMessage.isRequest()) {
            CommonResponse response = this.dispatch(rpcMessage, ctx);
            if (response != null) {
                InvokeService.writeResponse(rpcMessage, response, ctx);
            }
        } else {
            final long traceId = rpcMessage.getTraceId();
            invokeService.addResponse(traceId, rpcMessage);
        }
    }

    private CommonResponse dispatch(RPCMessage rpcMessage, ChannelHandlerContext ctx) {
        try {
            final String requestJSON = rpcMessage.getData();
            return switch (rpcMessage.getMethodType()) {
                case PRODUCER_REGISTER ->
                        producerService.register(JsonUtil.parseJson(requestJSON, RegisterRequest.class), ctx.channel());
                case PRODUCER_UNREGISTER ->
                        producerService.unregister(JsonUtil.parseJson(requestJSON, RegisterRequest.class), ctx.channel());
                case PRODUCER_SEND_MSG -> producerSendMessage(requestJSON, false);
                case PRODUCER_SEND_MSG_ONE_WAY -> producerSendMessage(requestJSON, true);
                case CONSUMER_REGISTER ->
                        consumerService.register(JsonUtil.parseJson(requestJSON, RegisterRequest.class), ctx.channel());
                case CONSUMER_UNREGISTER ->
                        consumerService.unregister(JsonUtil.parseJson(requestJSON, RegisterRequest.class), ctx.channel());
                case CONSUMER_SUB ->
                        consumerService.subscribe(JsonUtil.parseJson(requestJSON, ConsumerSubscribeRequest.class), ctx.channel());
                case CONSUMER_UNSUB ->
                        consumerService.unsubscribe(JsonUtil.parseJson(requestJSON, ConsumerSubscribeRequest.class), ctx.channel());
                case CONSUMER_HEARTBEAT ->
                        consumerService.heartbeat(JsonUtil.parseJson(requestJSON, HeartbeatRequest.class), ctx.channel());
                case CONSUMER_MSG_PULL ->
                        persistService.pull(JsonUtil.parseJson(requestJSON, ConsumerPullRequest.class), ctx.channel());
                default -> throw new UnsupportedOperationException("Request method type temporarily unsupported.");
            };
        } catch (Exception e) {
            return new CommonResponse(CommonResponseCode.FAIL.getCode(), CommonResponseCode.FAIL.getMessage());
        }
    }

    private CommonResponse producerSendMessage(String requestJSON, boolean sendOneWay) throws JsonProcessingException {
        Message message = JsonUtil.parseJson(requestJSON, Message.class);

        // 必须先持久化消息，因为异步发送消息方法将在此后更新持久化消息的状态
        MessagePO persistPutMsg = new MessagePO();
        persistPutMsg.setMessage(message);
        persistPutMsg.setConsumeStatus(ConsumeStatus.IDLE);
        CommonResponse response = persistService.save(persistPutMsg);

        List<GroupNameChannel> channelList = consumerService.listSubscribedConsumers(message);
        // 只有此主题+标签消息目前有消费者订阅时才推送
        if (!channelList.isEmpty()) {
            BrokerPushContext pushContext = BrokerPushContext.newInstance()
                    .setChannelList(channelList)
                    .setPersistenceService(persistService)
                    .setMessagePO(persistPutMsg)
                    .setInvokeService(invokeService)
                    .setResponseTimeout(responseTimeout);
            pushService.asyncPush(pushContext);
        }

        return sendOneWay ? null : response;
    }
}
