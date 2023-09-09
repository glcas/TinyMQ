package ind.sac.mq.broker.model.bo;

import ind.sac.mq.common.dto.Message;

import java.util.List;
import java.util.Objects;

public record PendingConsumer(Thread channelThread, List<Message> messages) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PendingConsumer) obj;
        return Objects.equals(this.channelThread, that.channelThread) &&
                Objects.equals(this.messages, that.messages);
    }

    @Override
    public String toString() {
        return "PendingConsumer[" +
                "channelThread=" + channelThread + ", " +
                "messages=" + messages + ']';
    }

}
