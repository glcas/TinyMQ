package ind.sac.mq.common.rpc;

import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.response.MQCommonResponseCode;

import java.io.Serializable;
import java.util.Objects;

/**
 * 本类是mq.common.dto中请求/响应类的封装，那些请求/响应类json化后存于本类的data字段 <p/>
 * 本类data字段存储的响应类已包含了业务逻辑得到的成功/失败结果
 * 故本类的响应字段（responseCode/responseMessage)与业务逻辑无关，只主动写入例如请求超时(TIMEOUT)这样的技术支持类型的响应信息
 * 但其存在的意义是为了将响应关键信息提取至RPC层级
 */
public class RPCMessage implements Serializable {

    private long time;

    private long traceId;

    private MethodType methodType;

    // 是否为请求信息
    private boolean isRequest;

    private String responseCode;

    private String responseMessage;

    private String data;

    public RPCMessage() {
    }

    public RPCMessage(long time, long traceId, MethodType methodType, boolean isRequest, String data) {
        this.time = time;
        this.traceId = traceId;
        this.methodType = methodType;
        this.isRequest = isRequest;
        this.data = data;
    }

    public RPCMessage(long time, long traceId, MethodType methodType, boolean isRequest, String responseCode, String responseMessage, String data) {
        this.time = time;
        this.traceId = traceId;
        this.methodType = methodType;
        this.isRequest = isRequest;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.data = data;
    }

    public static RPCMessage timeout() {
        RPCMessage dto = new RPCMessage();
        dto.setResponseCode(MQCommonResponseCode.TIMEOUT.getCode());
        dto.setResponseMessage(MQCommonResponseCode.TIMEOUT.getDescription());
        return dto;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTraceId() {
        return this.traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public MethodType getMethodType() {
        return this.methodType;
    }

    public void setMethodType(MethodType methodType) {
        this.methodType = methodType;
    }

    public boolean isRequest() {
        return this.isRequest;
    }

    public void setRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RPCMessage)) return false;
        final RPCMessage other = (RPCMessage) o;
        if (!other.canEqual(this)) return false;
        if (this.getTime() != other.getTime()) return false;
        final Object this$requestId = this.getTraceId();
        final Object other$requestId = other.getTraceId();
        if (!Objects.equals(this$requestId, other$requestId)) return false;
        final Object this$methodType = this.getMethodType();
        final Object other$methodType = other.getMethodType();
        if (!Objects.equals(this$methodType, other$methodType))
            return false;
        if (this.isRequest() != other.isRequest()) return false;
        final Object this$responseCode = this.getResponseCode();
        final Object other$responseCode = other.getResponseCode();
        if (!Objects.equals(this$responseCode, other$responseCode))
            return false;
        final Object this$responseMessage = this.getResponseMessage();
        final Object other$responseMessage = other.getResponseMessage();
        if (!Objects.equals(this$responseMessage, other$responseMessage))
            return false;
        final Object this$data = this.getData();
        final Object other$data = other.getData();
        return Objects.equals(this$data, other$data);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RPCMessage;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $time = this.getTime();
        result = result * PRIME + (int) ($time >>> 32 ^ $time);
        final Object $requestId = this.getTraceId();
        result = result * PRIME + $requestId.hashCode();
        final Object $methodType = this.getMethodType();
        result = result * PRIME + ($methodType == null ? 43 : $methodType.hashCode());
        result = result * PRIME + (this.isRequest() ? 79 : 97);
        final Object $responseCode = this.getResponseCode();
        result = result * PRIME + ($responseCode == null ? 43 : $responseCode.hashCode());
        final Object $responseMessage = this.getResponseMessage();
        result = result * PRIME + ($responseMessage == null ? 43 : $responseMessage.hashCode());
        final Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    public String toString() {
        return "RPCMessageDTO(time=" + this.getTime() + ", requestId=" + this.getTraceId() + ", methodType=" + this.getMethodType() + ", isRequest=" + this.isRequest() + ", responseCode=" + this.getResponseCode() + ", responseMessage=" + this.getResponseMessage() + ", data=" + this.getData() + ")";
    }
}
