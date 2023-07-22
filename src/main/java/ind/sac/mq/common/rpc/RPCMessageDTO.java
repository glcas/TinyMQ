package ind.sac.mq.common.rpc;

import ind.sac.mq.common.exception.MQCommonResponseCode;

import java.io.Serializable;
import java.util.Objects;

/**
 * 是mq.common.dto中请求/响应类的封装，将其json化放至data中 <p/>
 * data中响应类已包含了逻辑得到的成功/失败标志
 * 故rpc-dto的响应码只在是响应且
 */
public class RPCMessageDTO implements Serializable {

    private long time;

    private long requestId;

    private String methodType;

    // 是否为请求信息
    private boolean isRequest;

    private String responseCode;

    private String responseMessage;

    private String data;

    public RPCMessageDTO() {
    }

    public static RPCMessageDTO timeout() {
        RPCMessageDTO dto = new RPCMessageDTO();
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

    public long getRequestId() {
        return this.requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getMethodType() {
        return this.methodType;
    }

    public void setMethodType(String methodType) {
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
        if (!(o instanceof RPCMessageDTO)) return false;
        final RPCMessageDTO other = (RPCMessageDTO) o;
        if (!other.canEqual(this)) return false;
        if (this.getTime() != other.getTime()) return false;
        final Object this$requestId = this.getRequestId();
        final Object other$requestId = other.getRequestId();
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
        return other instanceof RPCMessageDTO;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $time = this.getTime();
        result = result * PRIME + (int) ($time >>> 32 ^ $time);
        final Object $requestId = this.getRequestId();
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
        return "RPCMessageDTO(time=" + this.getTime() + ", requestId=" + this.getRequestId() + ", methodType=" + this.getMethodType() + ", isRequest=" + this.isRequest() + ", responseCode=" + this.getResponseCode() + ", responseMessage=" + this.getResponseMessage() + ", data=" + this.getData() + ")";
    }
}
