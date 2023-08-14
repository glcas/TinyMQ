package ind.sac.mq.broker.dto.consumer;

import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.MQCommonRequest;

public class ConsumerSubscribeRequest extends MQCommonRequest {

    private String groupName;

    private String topicName;

    private String tagRegex;

    private String consumerType;

    public ConsumerSubscribeRequest() {
    }

    public ConsumerSubscribeRequest(long traceId, MethodType methodType, String groupName, String topicName, String tagRegex) {
        super(traceId, methodType);
        this.groupName = groupName;
        this.topicName = topicName;
        this.tagRegex = tagRegex;
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

    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }
}
