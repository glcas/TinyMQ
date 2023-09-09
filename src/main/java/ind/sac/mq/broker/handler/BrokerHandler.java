package ind.sac.mq.broker.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import ind.sac.mq.consumer.dto.request.ConsumeStatusUpdateRequest;
import ind.sac.mq.consumer.dto.request.ConsumerSubscribeRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrokerHandler extends SimpleChannelInboundHandler {

    private final Logger logger = LoggerFactory.getLogger(BrokerHandler.class);

    private final InvokeService invokeService;

    private final BrokerConsumerService consumerService;

    private final BrokerProducerService producerService;

    private final BrokerPersistenceService persistenceService;

    private final BrokerPushService pushService;

    private final long responseTimeout;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public BrokerHandler(InvokeService invokeService, BrokerConsumerService consumerService, BrokerProducerService producerService, BrokerPersistenceService persistenceService, BrokerPushService pushService, long responseTimeout) {
        this.invokeService = invokeService;
        this.consumerService = consumerService;
        this.producerService = producerService;
        this.persistenceService = persistenceService;
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
            executorService.submit(() -> {
                CommonResponse response = this.dispatch(rpcMessage, ctx);
                try {
                    InvokeService.writeResponse(rpcMessage, response, ctx);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
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
                case CONSUMER_LONG_POLLING ->
                        consumerService.longPolling(requestJSON, persistenceService.getMessageByTopicMap(), ctx.channel());
                case CONSUMER_PULL_ACK -> {
                    ConsumeStatusUpdateRequest consumeStatusUpdateRequest = JsonUtil.parseJson(requestJSON, ConsumeStatusUpdateRequest.class);
                    yield persistenceService.updateStatus(consumeStatusUpdateRequest.getMessageTraceID(), consumeStatusUpdateRequest.getConsumeStatus());
                }
                default -> throw new UnsupportedOperationException("Request method type temporarily unsupported.");
            };
        } catch (Exception e) {
            return new CommonResponse(CommonResponseCode.FAIL.getCode(), CommonResponseCode.FAIL.getMessage());
        }
    }

    private CommonResponse producerSendMessage(String requestJSON, boolean sendOneWay) throws JsonProcessingException {
        Message message = JsonUtil.parseJson(requestJSON, Message.class);

        // 必须先持久化消息，因为异步发送消息方法将在此后更新持久化消息的状态
        MessagePO messagePO = new MessagePO();
        messagePO.setMessage(message);
        messagePO.setConsumeStatus(ConsumeStatus.IDLE);
        CommonResponse response = persistenceService.save(messagePO);

        consumerService.notifyPendingConsumers(messagePO);

        return sendOneWay ? null : response;
    }
}
