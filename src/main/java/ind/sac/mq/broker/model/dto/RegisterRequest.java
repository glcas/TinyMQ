package ind.sac.mq.broker.model.dto;

import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.CommonRequest;

public class RegisterRequest extends CommonRequest {

    private String groupName;

    private int weight;

    public RegisterRequest() {
    }

    public RegisterRequest(long traceId, MethodType methodType, String groupName, int weight) {
        super(traceId, methodType);
        this.groupName = groupName;
        this.weight = weight;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getWeight() {
        return weight;
    }
}
