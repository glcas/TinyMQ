package ind.sac.mq.common.dto.request;

import ind.sac.mq.common.constant.MethodType;

public class HeartbeatRequest extends CommonRequest {

    // 非必要传输信息，主要用于占位
    private String groupName;

    public HeartbeatRequest() {
    }

    public HeartbeatRequest(long traceId, MethodType methodType, String groupName) {
        super(traceId, methodType);
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
