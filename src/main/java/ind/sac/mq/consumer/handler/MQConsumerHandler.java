package ind.sac.mq.consumer.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.dto.response.MQConsumeResultResponse;
import ind.sac.mq.common.response.ConsumeStatus;
import ind.sac.mq.common.response.MQCommonResponseCode;
import ind.sac.mq.common.rpc.RPCMessage;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.utils.DelimiterUtil;
import ind.sac.mq.common.utils.JsonUtil;
import ind.sac.mq.consumer.api.IMQConsumerListenerService;
import ind.sac.mq.consumer.support.listener.MQConsumerListenerContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class MQConsumerHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(MQConsumerHandler.class);

    private final IInvokeService invokeService;

    private final IMQConsumerListenerService listenerService;

    public MQConsumerHandler(IInvokeService invokeService, IMQConsumerListenerService listenerService) {
        this.invokeService = invokeService;
        this.listenerService = listenerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        ByteBuf byteBuf = (ByteBuf) o;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        RPCMessage rpcMessage = JsonUtil.parseJson(bytes, RPCMessage.class);

        // 如果接收到的是请求，直接构造响应并写回
        // 否则是接收到的响应，存储之到自己的响应map中
        if (rpcMessage.isRequest()) {
            MQCommonResponse commonResponse = this.msgProcess(rpcMessage, channelHandlerContext);
            this.writeResponse(rpcMessage, commonResponse, channelHandlerContext);
        } else {
            final long requestId = rpcMessage.getTraceId();
            invokeService.addResponse(requestId, rpcMessage);
        }

    }

    private MQCommonResponse msgProcess(RPCMessage rpcMessage, ChannelHandlerContext channelHandlerContext) {
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

    private MQCommonResponse consume(String mqMsgStr) {
        try {
            MQMessage message = JsonUtil.parseJson(mqMsgStr, MQMessage.class);
            ConsumeStatus status = this.listenerService.consume(message, new MQConsumerListenerContext());
            return new MQConsumeResultResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription(), status.getCode());
        } catch (Exception e) {
            logger.error("Consumed message error.", e);
            return new MQConsumeResultResponse(MQCommonResponseCode.FAIL.getCode(), MQCommonResponseCode.FAIL.getDescription());
        }
    }


    private void writeResponse(RPCMessage request, MQCommonResponse commonResponse, ChannelHandlerContext channelHandlerContext) throws JsonProcessingException {
        RPCMessage rpcMessage = new RPCMessage();

        // 设置响应头
        rpcMessage.setRequest(false);
        rpcMessage.setTraceId(request.getTraceId());
        rpcMessage.setMethodType(request.getMethodType());  // 方法类型与响应一致，因为响应是捆绑于请求的
        rpcMessage.setTime(System.currentTimeMillis());
        rpcMessage.setResponseCode(commonResponse.getResponseCode());
        rpcMessage.setResponseMessage(commonResponse.getResponseMessage());

        // 实际响应体json化
        String data = JsonUtil.writeAsJsonString(commonResponse);
        rpcMessage.setData(data);

        // 字节化并发送
        ByteBuf byteBuf = DelimiterUtil.getDelimitedMessageBuffer(rpcMessage);
        channelHandlerContext.writeAndFlush(byteBuf);
    }

    public IInvokeService getInvokeService() {
        return this.invokeService;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MQConsumerHandler)) return false;
        final MQConsumerHandler other = (MQConsumerHandler) o;
        if (!other.canEqual(this)) return false;
        final Object this$invokeService = this.getInvokeService();
        final Object other$invokeService = other.getInvokeService();
        return Objects.equals(this$invokeService, other$invokeService);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MQConsumerHandler;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $invokeService = this.getInvokeService();
        result = result * PRIME + ($invokeService == null ? 43 : $invokeService.hashCode());
        return result;
    }

    public String toString() {
        return "MQConsumerHandler(invokeService=" + this.getInvokeService() + ")";
    }
}
