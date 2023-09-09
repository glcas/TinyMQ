package ind.sac.mq.consumer.record;

import java.util.Objects;

/**
 * 消费者发送订阅请求的实质性内容，包括话题名和标签匹配正则
 */
public record SubscribeInfo(String topicName, String tagRegex) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SubscribeInfo) obj;
        return Objects.equals(this.topicName, that.topicName) &&
                Objects.equals(this.tagRegex, that.tagRegex);
    }

    @Override
    public String toString() {
        return "SubscribeInfo[" +
                "topicName=" + topicName + ", " +
                "tagRegex=" + tagRegex + ']';
    }

}
