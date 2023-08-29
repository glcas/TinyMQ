package ind.sac.mq.producer.dto;

import ind.sac.mq.producer.constant.SendStatus;

import java.util.Objects;

public class SendResult {

    /**
     * Unique Id
     */
    private long traceId;

    private SendStatus status;

    public SendResult() {
    }

    public static SendResult of(long messageId, SendStatus status) {
        SendResult result = new SendResult();
        result.setTraceId(messageId);
        result.setStatus(status);
        return result;
    }

    public long getTraceId() {
        return this.traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public SendStatus getStatus() {
        return this.status;
    }

    public void setStatus(SendStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendResult that = (SendResult) o;
        return traceId == that.traceId && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceId, status);
    }

    @Override
    public String toString() {
        return "SendResult{" +
                "traceId=" + traceId +
                ", status=" + status +
                '}';
    }
}
