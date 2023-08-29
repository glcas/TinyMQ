package ind.sac.mq.consumer.dto.request;

import ind.sac.mq.common.dto.request.CommonRequest;

public class ConsumerPullRequest extends CommonRequest {

    private String groupName;

    private int pullSize;

    private String topicName;

    private String tagRegex;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getPullSize() {
        return pullSize;
    }

    public void setPullSize(int pullSize) {
        this.pullSize = pullSize;
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

    @Override
    public String toString() {
        return "ConsumerPullRequest{" +
                "groupName='" + groupName + '\'' +
                ", pullSize=" + pullSize +
                ", topicName='" + topicName + '\'' +
                ", tagRegex='" + tagRegex + '\'' +
                '}';
    }
}
