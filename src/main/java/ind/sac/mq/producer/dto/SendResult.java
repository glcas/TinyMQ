package ind.sac.mq.producer.dto;

import ind.sac.mq.producer.constant.SendStatus;

import java.util.Objects;

public class SendResult {

    /**
     * Unique Id
     */
    private long messageId;

    private SendStatus status;

    public SendResult() {
    }

    public static SendResult of(long messageId, SendStatus status) {
        SendResult result = new SendResult();
        result.setMessageId(messageId);
        result.setStatus(status);
        return result;
    }

    public long getMessageId() {
        return this.messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public SendStatus getStatus() {
        return this.status;
    }

    public void setStatus(SendStatus status) {
        this.status = status;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SendResult)) return false;
        final SendResult other = (SendResult) o;
        if (!other.canEqual(this)) return false;
        final Object this$messageId = this.getMessageId();
        final Object other$messageId = other.getMessageId();
        if (!Objects.equals(this$messageId, other$messageId)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        return Objects.equals(this$status, other$status);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SendResult;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $messageId = this.getMessageId();
        result = result * PRIME + $messageId.hashCode();
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        return result;
    }

    public String toString() {
        return "SendResult(messageId=" + this.getMessageId() + ", status=" + this.getStatus() + ")";
    }
}
