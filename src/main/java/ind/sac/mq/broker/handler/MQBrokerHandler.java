package ind.sac.mq.broker.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.api.IBrokerConsumerService;
import ind.sac.mq.broker.api.IBrokerProducerService;
import ind.sac.mq.broker.api.IBrokerPushService;
import ind.sac.mq.broker.api.IMQBrokerPersistService;
import ind.sac.mq.broker.dto.BrokerRegisterRequest;
import ind.sac.mq.broker.dto.consumer.ConsumerSubscribeRequest;
import ind.sac.mq.broker.dto.persist.MQPersistPutMsg;
import ind.sac.mq.broker.support.GroupNameChannel;
import ind.sac.mq.broker.support.push.BrokerPushContext;
import ind.sac.mq.common.constant.MessageStatusConst;
import ind.sac.mq.common.dto.request.MQConsumerPullRequest;
import ind.sac.mq.common.dto.request.MQRequestMessage;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.response.MQCommonResponseCode;
import ind.sac.mq.common.rpc.RPCMessage;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.utils.DelimiterUtil;
import ind.sac.mq.common.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

public class MQBrokerHandler extends SimpleChannelInboundHandler {

    private final IInvokeService invokeService;

    private final IBrokerConsumerService consumerService;

    private final IBrokerProducerService producerService;

    private final IMQBrokerPersistService persistService;

    private final IBrokerPushService pushService;

    private final long responseTimeout;

    public MQBrokerHandler(IInvokeService invokeService, IBrokerConsumerService consumerService, IBrokerProducerService producerService, IMQBrokerPersistService persistService, IBrokerPushService pushService, long responseTimeout) {
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
            MQCommonResponse response = this.dispatch(rpcMessage, ctx);
            if (response != null) {
                this.writeResponse(response, rpcMessage, ctx);
            }
        } else {
            final long traceId = rpcMessage.getTraceId();
            invokeService.addResponse(traceId, rpcMessage);
        }
    }

    private MQCommonResponse dispatch(RPCMessage rpcMessage, ChannelHandlerContext ctx) {
        try {
            final String requestJSON = rpcMessage.getData();
            switch (rpcMessage.getMethodType()) {
                case PRODUCER_REGISTER:
                    return producerService.register(JsonUtil.parseJson(requestJSON, BrokerRegisterRequest.class).getServiceEntry(), ctx.channel());
                case PRODUCER_UNREGISTER:
                    return producerService.unRegister(JsonUtil.parseJson(requestJSON, BrokerRegisterRequest.class).getServiceEntry(), ctx.channel());
                case PRODUCER_SEND_MSG:
                    return producerSendMessage(requestJSON, false);
                case PRODUCER_SEND_MSG_ONE_WAY:
                    return producerSendMessage(requestJSON, true);
                case CONSUMER_REGISTER:
                    return consumerService.register(JsonUtil.parseJson(requestJSON, BrokerRegisterRequest.class).getServiceEntry(), ctx.channel());
                case CONSUMER_UNREGISTER:
                    return consumerService.unregister(JsonUtil.parseJson(requestJSON, BrokerRegisterRequest.class).getServiceEntry(), ctx.channel());
                case CONSUMER_SUB:
                    return consumerService.subscribe(JsonUtil.parseJson(requestJSON, ConsumerSubscribeRequest.class), ctx.channel());
                case CONSUMER_UNSUB:
                    return consumerService.unsubscribe(JsonUtil.parseJson(requestJSON, ConsumerSubscribeRequest.class), ctx.channel());
                case CONSUMER_MSG_PULL:
                    return persistService.pull(JsonUtil.parseJson(requestJSON, MQConsumerPullRequest.class), ctx.channel());
                default:
                    throw new UnsupportedOperationException("Request method type temporarily unsupported.");
            }
        } catch (Exception e) {
            return new MQCommonResponse(MQCommonResponseCode.FAIL.getCode(), MQCommonResponseCode.FAIL.getDescription());
        }
    }

    private MQCommonResponse producerSendMessage(String requestJSON, boolean sendOneWay) throws JsonProcessingException {
        MQRequestMessage requestMessage = JsonUtil.parseJson(requestJSON, MQRequestMessage.class);

        // 必须先持久化消息，因为异步发送消息方法将在此后更新持久化消息的状态
        MQPersistPutMsg persistPutMsg = new MQPersistPutMsg();
        persistPutMsg.setMqRequestMessage(requestMessage);
        persistPutMsg.setMsgStatus(MessageStatusConst.WAIT_CONSUME);
        MQCommonResponse response = persistService.put(persistPutMsg);

        List<GroupNameChannel> channelList = consumerService.getPushedSubscribeList(requestMessage);
        if (!channelList.isEmpty()) {
            // 只有此主题+标签消息目前有消费者订阅时才推送
            BrokerPushContext pushContext = BrokerPushContext.newInstance()
                    .setChannelList(channelList)
                    .setMQBrokerPersist(persistService)
                    .setMQPersistPutMsg(persistPutMsg)
                    .setInvokeService(invokeService)
                    .setResponseTimeoutMs(responseTimeout);
            pushService.asyncPush(pushContext);
        }

        return sendOneWay ? null : response;
    }

    private void writeResponse(MQCommonResponse response, RPCMessage reqRpcMsg, ChannelHandlerContext ctx) throws JsonProcessingException {
        RPCMessage rpcMessage = new RPCMessage(System.currentTimeMillis(), reqRpcMsg.getTraceId(), reqRpcMsg.getMethodType(), false, response.getResponseCode(), response.getResponseMessage(), JsonUtil.writeAsJsonString(response));
        ByteBuf byteBuf = DelimiterUtil.getDelimitedMessageBuffer(rpcMessage);
        ctx.writeAndFlush(byteBuf);
    }
}
