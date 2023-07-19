package ind.sac.mq.consumer.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.exception.MQCommonResponseCode;
import ind.sac.mq.common.rpc.RPCMessageDTO;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.utils.DelimiterUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class MQConsumerHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(MQConsumerHandler.class);

    private final IInvokeService invokeService;

    public MQConsumerHandler(IInvokeService invokeService) {
        this.invokeService = invokeService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        ByteBuf byteBuf = (ByteBuf) o;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        ObjectMapper objectMapper = new ObjectMapper();
        RPCMessageDTO rpcMessageDTO = objectMapper.readValue(bytes, RPCMessageDTO.class);

        // 如果接收到的是请求，直接构造响应并写回
        // 否则是接收到的响应，存储之到自己的响应map中
        if (rpcMessageDTO.isRequest()) {
            MQCommonResponse commonResponse = this.dispatch(rpcMessageDTO, channelHandlerContext);
            this.writeResponse(rpcMessageDTO, commonResponse, channelHandlerContext);
        } else {
            final String requestId = rpcMessageDTO.getRequestId();
            if (requestId.isEmpty()) {
                return;
            }
            invokeService.addResponse(requestId, rpcMessageDTO);
        }

    }

    private MQCommonResponse dispatch(RPCMessageDTO rpcMessageDTO, ChannelHandlerContext channelHandlerContext) {
        final String methodType = rpcMessageDTO.getMethodType();
        final String data = rpcMessageDTO.getData();
        String channelId = channelHandlerContext.channel().id().asLongText();
        logger.debug("channelId: {} - Received method {} contents {}.", channelId, methodType, data);
        if (MethodType.PRODUCER_SEND_MESSAGE.equals(methodType)) {
            logger.info("Received from producer: {}", data);
            // TODO: really handle received data
            MQCommonResponse response = new MQCommonResponse();
            response.setResponseCode(MQCommonResponseCode.SUCCESS.getCode());
            response.setResponseMessage(MQCommonResponseCode.SUCCESS.getDescription());
            return response;
        }
        throw new UnsupportedOperationException("Method type temporarily unsupported.");
    }


    private void writeResponse(RPCMessageDTO request, MQCommonResponse commonResponse, ChannelHandlerContext channelHandlerContext) throws JsonProcessingException {
        RPCMessageDTO rpcMessageDTO = new RPCMessageDTO();

        // 设置响应头
        rpcMessageDTO.setRequest(false);
        rpcMessageDTO.setRequestId(request.getRequestId());
        rpcMessageDTO.setMethodType(request.getMethodType());  // 方法类型与响应一致，因为响应是捆绑于请求的
        rpcMessageDTO.setTime(System.currentTimeMillis());

        // 实际响应体json化
        ObjectMapper objectMapper = new ObjectMapper();
        String data = objectMapper.writeValueAsString(commonResponse);
        rpcMessageDTO.setData(data);

        // 字节化
        ByteBuf byteBuf = DelimiterUtil.getDelimitedMessageBuffer(rpcMessageDTO);
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
