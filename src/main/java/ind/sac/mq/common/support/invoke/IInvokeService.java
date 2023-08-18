package ind.sac.mq.common.support.invoke;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.dto.request.MQCommonRequest;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.MQCommonResponseCode;
import ind.sac.mq.common.rpc.RPCMessage;
import ind.sac.mq.common.utils.DelimiterUtil;
import ind.sac.mq.common.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface IInvokeService {

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
     * @param <T>           MQCommonRequest
     * @param <R>           MQCommonResponse
     * @return 响应
     * @throws JsonProcessingException JSON解析异常
     */
    static <T extends MQCommonRequest, R extends MQCommonResponse> R callServer(Channel channel, T req, Class<R> responseClass, IInvokeService invokeService, long timeout) throws JsonProcessingException {
        // 构造RPC消息
        final long traceId = req.getTraceId();
        RPCMessage rpcMessage = new RPCMessage(System.currentTimeMillis(), traceId, req.getMethodType(), true, JsonUtil.writeAsJsonString(req));

        // 本地记录该请求
        invokeService.addRequest(traceId, timeout);

        // 序列化并发送
        ByteBuf byteBuf = DelimiterUtil.getDelimitedMessageBuffer(rpcMessage);
        channel.writeAndFlush(byteBuf);

        // 响应处理
        if (responseClass == null) {
            // callServer方法调用时传入的响应类型为无，意味着这次调用发送的消息是单向的，不需要响应
            return null;
        } else {
            // 响应获取，getResponse方法在响应map中查id，查不到时在此等待
            RPCMessage response = invokeService.getResponse(traceId);
            if (response.getResponseCode().equals(MQCommonResponseCode.TIMEOUT.getCode())) {
                throw new MQException(MQCommonResponseCode.TIMEOUT);
            }
            return JsonUtil.parseJson(response.getData(), responseClass);
        }
    }
}
