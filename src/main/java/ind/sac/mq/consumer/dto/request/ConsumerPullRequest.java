package ind.sac.mq.consumer.dto.request;

import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.CommonRequest;
import ind.sac.mq.consumer.record.SubscribeInfo;

import java.util.Set;

public class ConsumerPullRequest extends CommonRequest {

    private String groupName;

    private int pullSize;

    private Set<SubscribeInfo> subscribeInfos;

    public ConsumerPullRequest(long traceId, MethodType methodType, String groupName, int pullSize, Set<SubscribeInfo> subscribeInfos) {
        super(traceId, methodType);
        this.groupName = groupName;
        this.pullSize = pullSize;
        this.subscribeInfos = subscribeInfos;
    }

    public ConsumerPullRequest() {
    }

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

    public Set<SubscribeInfo> getSubscribeInfos() {
        return subscribeInfos;
    }
}
