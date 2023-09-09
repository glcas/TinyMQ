package ind.sac.mq.consumer.handler;

import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.common.response.CommonResponseCode;
import ind.sac.mq.common.rpc.RPCMessage;
import ind.sac.mq.common.service.invoke.InvokeService;
import ind.sac.mq.common.util.JsonUtil;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import ind.sac.mq.consumer.dto.response.ConsumeResponse;
import ind.sac.mq.consumer.service.listener.ConsumerListenerService;
import ind.sac.mq.consumer.service.listener.impl.ConsumerListenerContextImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class ConsumerHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    private final InvokeService invokeService;

    private final ConsumerListenerService listenerService;

    public ConsumerHandler(InvokeService invokeService, ConsumerListenerService listenerService) {
        this.invokeService = invokeService;
        this.listenerService = listenerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        ByteBuf byteBuf = (ByteBuf) o;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        RPCMessage rpcMessage = JsonUtil.parseJson(bytes, RPCMessage.class);

        // 如果接收到的是请求，构造响应并写回
        // 否则是接收到的响应，存储之到自己的响应map中
        if (rpcMessage.isRequest()) {
            CommonResponse response = this.process(rpcMessage, channelHandlerContext);
            InvokeService.writeResponse(rpcMessage, response, channelHandlerContext);
        } else {
            final long traceId = rpcMessage.getTraceId();
            invokeService.addResponse(traceId, rpcMessage);
        }

    }

    private CommonResponse process(RPCMessage rpcMessage, ChannelHandlerContext channelHandlerContext) {
        final MethodType methodType = rpcMessage.getMethodType();
        final String data = rpcMessage.getData();
        String channelId = channelHandlerContext.channel().id().asLongText();
        logger.debug("channelId: {} - Received method {} contents {}.", channelId, methodType, data);
        if (methodType == MethodType.BROKER_MSG_PUSH) {
            logger.info("Received message: {}", data);
            // consume message
            return this.consume(data);
        }
        throw new UnsupportedOperationException("Method type temporarily unsupported.");
    }

    private CommonResponse consume(String mqMsgStr) {
        try {
            Message message = JsonUtil.parseJson(mqMsgStr, Message.class);
            ConsumeStatus status = this.listenerService.consume(message, new ConsumerListenerContextImpl());
            return new ConsumeResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage(), status);
        } catch (Exception e) {
            logger.error("Consumed message error.", e);
            return new ConsumeResponse(CommonResponseCode.FAIL.getCode(), CommonResponseCode.FAIL.getMessage());
        }
    }

    public InvokeService getInvokeService() {
        return this.invokeService;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ConsumerHandler other)) return false;
        if (!other.canEqual(this)) return false;
        final Object this$invokeService = this.getInvokeService();
        final Object other$invokeService = other.getInvokeService();
        return Objects.equals(this$invokeService, other$invokeService);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ConsumerHandler;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $invokeService = this.getInvokeService();
        result = result * PRIME + ($invokeService == null ? 43 : $invokeService.hashCode());
        return result;
    }

    public String toString() {
        return "ConsumerHandler(invokeService=" + this.getInvokeService() + ")";
    }
}
