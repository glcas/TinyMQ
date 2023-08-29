package ind.sac.mq.common.service.invoke;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.dto.request.CommonRequest;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.CommonResponseCode;
import ind.sac.mq.common.rpc.RPCMessage;
import ind.sac.mq.common.util.DelimiterUtil;
import ind.sac.mq.common.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public interface InvokeService {

    /**
     * add request
     *
     * @param sequenceId sequence ID
     * @param timeout    timeout milliseconds
     */
    void addRequest(final long sequenceId, final long timeout);

    void addResponse(final long sequenceId, final RPCMessage rpcResponse) throws JsonProcessingException;

    /**
     * 查看是否仍包含待处理请求
     *
     * @return 布尔结果
     */
    boolean remainsRequest();

    RPCMessage getResponse(final long sequenceId);


    /**
     * 泛型T,R的上界由extends确定
     *
     * @param channel       通信连接通道
     * @param req           请求
     * @param responseClass 响应类
     * @param invokeService 调用服务实例
     * @param timeout       设定的超时时间（毫秒）
     * @param <T>           CommonRequest
     * @param <R>           CommonResponse
     * @return 响应
     * @throws JsonProcessingException JSON解析异常
     */
    static <T extends CommonRequest, R extends CommonResponse> R callServer(Channel channel, T req, Class<R> responseClass, InvokeService invokeService, long timeout) throws JsonProcessingException {
        // 构造RPC消息
        final long traceId = req.getTraceId();
        RPCMessage rpcMessage = new RPCMessage(System.currentTimeMillis(), traceId, req.getMethodType(), true, JsonUtil.writeAsJsonString(req));

        // 序列化并发送
        ByteBuf byteBuf = DelimiterUtil.getRPCMessageWithDelimiterBuffer(rpcMessage);
        channel.writeAndFlush(byteBuf);

        // 响应处理
        if (responseClass == null) {
            // callServer方法调用时传入的响应类型为无，意味着这次调用发送的消息是单向的，不需要响应
            return null;
        } else {
            // 本地记录该请求，以便后续监控响应
            invokeService.addRequest(traceId, timeout);
            // 响应获取，getResponse方法在响应map中查id，查不到时在此等待
            RPCMessage response = invokeService.getResponse(traceId);
            if (response.getResponseCode().equals(CommonResponseCode.TIMEOUT.getCode())) {
                throw new MQException(CommonResponseCode.TIMEOUT);
            }
            return JsonUtil.parseJson(response.getData(), responseClass);
        }
    }

    static void writeResponse(RPCMessage request, CommonResponse response, ChannelHandlerContext ctx) throws JsonProcessingException {
        RPCMessage rpcMessage = new RPCMessage();

        // 设置响应头
        rpcMessage.setRequest(false);
        rpcMessage.setTraceId(request.getTraceId());
        rpcMessage.setMethodType(request.getMethodType());  // 方法类型与响应一致，因为响应是捆绑于请求的
        rpcMessage.setTime(System.currentTimeMillis());
        rpcMessage.setResponseCode(response.getResponseCode());
        rpcMessage.setResponseMessage(response.getResponseMessage());

        // 实际响应体json化
        String data = JsonUtil.writeAsJsonString(response);
        rpcMessage.setData(data);

        // 字节化并发送
        ByteBuf byteBuf = DelimiterUtil.getRPCMessageWithDelimiterBuffer(rpcMessage);
        ctx.writeAndFlush(byteBuf);
    }
}
