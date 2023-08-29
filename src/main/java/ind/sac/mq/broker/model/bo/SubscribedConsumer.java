package ind.sac.mq.broker.model.bo;

import java.util.Objects;

/**
 * Includes info of group name, topic name, tag regex, channel id and url(extends from RPCAddress).
 */
public class SubscribedConsumer {

    private String groupName;

    private String topicName;

    private String tagRegex;

    private String channelId;

    public SubscribedConsumer(String groupName, String topicName, String tagRegex, String channelId) {
        this.groupName = groupName;
        this.topicName = topicName;
        this.tagRegex = tagRegex;
        this.channelId = channelId;
    }

    public SubscribedConsumer() {
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTagRegex() {
        return tagRegex;
    }

    public void setTagRegex(String tagRegex) {
        this.tagRegex = tagRegex;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscribedConsumer that = (SubscribedConsumer) o;
        return Objects.equals(groupName, that.groupName) && Objects.equals(topicName, that.topicName) && Objects.equals(tagRegex, that.tagRegex) && Objects.equals(channelId, that.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, topicName, tagRegex, channelId);
    }
}
