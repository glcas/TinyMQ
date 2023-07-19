package ind.sac.mq.producer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import ind.sac.mq.common.rpc.RPCMessageDTO;
import ind.sac.mq.common.support.invoke.IInvokeService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQProducerHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(MQProducerHandler.class);

    private IInvokeService invokeService;

    public void setInvokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        ByteBuf byteBuf = (ByteBuf) o;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        logger.debug("[Producer] ChannelId {} received message: {}.", channelHandlerContext.channel().id().asLongText(), new String(bytes));

        ObjectMapper objectMapper = new ObjectMapper();
        RPCMessageDTO rpcMessageDTO = objectMapper.readValue(bytes, RPCMessageDTO.class);
        if (rpcMessageDTO.isRequest()) {
            final String methodType = rpcMessageDTO.getMethodType();
            final String data = rpcMessageDTO.getData();
        } else {
            if (rpcMessageDTO.getRequestId().isEmpty()) {
                // deprecate response whose request id is empty
                return;
            }
            invokeService.addResponse(rpcMessageDTO.getRequestId(), rpcMessageDTO);
        }
    }
}
