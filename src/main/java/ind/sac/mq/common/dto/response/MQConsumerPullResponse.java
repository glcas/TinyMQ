package ind.sac.mq.common.dto.response;

import ind.sac.mq.common.dto.request.MQRequestMessage;

import java.util.List;

public class MQConsumerPullResponse extends MQCommonResponse {

    private List<MQRequestMessage> list;

    public List<MQRequestMessage> getList() {
        return list;
    }

    public void setList(List<MQRequestMessage> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "MQConsumerPullResponse{" +
                "list=" + list +
                '}';
    }
}
