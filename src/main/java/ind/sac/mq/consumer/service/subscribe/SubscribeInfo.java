package ind.sac.mq.consumer.service.subscribe;

import java.util.Objects;

/**
 * 消费者发送订阅请求的实质性内容，包括话题名和标签匹配正则
 */
public record SubscribeInfo(String topicName, String tagRegex) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscribeInfo that = (SubscribeInfo) o;
        return Objects.equals(topicName, that.topicName) && Objects.equals(tagRegex, that.tagRegex);
    }

}
