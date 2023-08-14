package ind.sac.mq.common.dto.request;

import ind.sac.mq.common.constant.MethodType;

import java.io.Serializable;
import java.util.Objects;

public class MQCommonRequest implements Serializable {
    private long traceId;
    private MethodType methodType;

    public MQCommonRequest() {
    }

    public MQCommonRequest(long traceId, MethodType methodType) {
        this.traceId = traceId;
        this.methodType = methodType;
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
