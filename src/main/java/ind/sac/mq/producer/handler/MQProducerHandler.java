package ind.sac.mq.producer.handler;

import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.rpc.RPCMessage;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQProducerHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(MQProducerHandler.class);

    private IInvokeService invokeService;


    public MQProducerHandler() {
    }

    public MQProducerHandler(IInvokeService invokeService) {
        this.invokeService = invokeService;
    }

    public void setInvokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        ByteBuf byteBuf = (ByteBuf) o;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        logger.debug("[Producer] ChannelId {} received message: {}.", channelHandlerContext.channel().id().asLongText(), new String(bytes));

        RPCMessage rpcMessage = JsonUtil.parseJson(bytes, RPCMessage.class);
        if (rpcMessage.isRequest()) {
            final MethodType methodType = rpcMessage.getMethodType();
            final String data = rpcMessage.getData();
        } else {
            invokeService.addResponse(rpcMessage.getTraceId(), rpcMessage);
        }
    }
}
