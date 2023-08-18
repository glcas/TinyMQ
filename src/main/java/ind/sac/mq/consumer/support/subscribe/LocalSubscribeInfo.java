package ind.sac.mq.consumer.support.subscribe;

import java.util.Objects;

/**
 * 消费者发送订阅请求的实质性内容，包括话题名和标签匹配正则
 */
public class LocalSubscribeInfo {

    private final String topicName;

    private final String tagRegex;

    public LocalSubscribeInfo(String topicName, String tagRegex) {
        this.topicName = topicName;
        this.tagRegex = tagRegex;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getTagRegex() {
        return tagRegex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscribeInfo that = (LocalSubscribeInfo) o;
        return Objects.equals(topicName, that.topicName) && Objects.equals(tagRegex, that.tagRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, tagRegex);
    }
}
