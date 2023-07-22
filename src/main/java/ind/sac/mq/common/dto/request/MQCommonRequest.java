package ind.sac.mq.common.dto.request;

import java.io.Serializable;
import java.util.Objects;

public class MQCommonRequest extends MQMessage implements Serializable {
    private long traceId;
    private String methodType;

    public MQCommonRequest() {
    }

    public long getTraceId() {
        return this.traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public String getMethodType() {
        return this.methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MQCommonRequest)) return false;
        final MQCommonRequest other = (MQCommonRequest) o;
        if (!other.canEqual(this)) return false;
        final Object this$traceId = this.getTraceId();
        final Object other$traceId = other.getTraceId();
        if (!Objects.equals(this$traceId, other$traceId)) return false;
        final Object this$methodType = this.getMethodType();
        final Object other$methodType = other.getMethodType();
        return Objects.equals(this$methodType, other$methodType);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MQCommonRequest;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $traceId = this.getTraceId();
        result = result * PRIME + $traceId.hashCode();
        final Object $methodType = this.getMethodType();
        result = result * PRIME + ($methodType == null ? 43 : $methodType.hashCode());
        return result;
    }

    public String toString() {
        return "MQCommonRequest(traceId=" + this.getTraceId() + ", methodType=" + this.getMethodType() + ")";
    }
}
