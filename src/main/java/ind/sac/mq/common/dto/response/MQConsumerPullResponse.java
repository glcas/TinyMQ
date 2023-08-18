package ind.sac.mq.common.dto.response;

import ind.sac.mq.common.dto.request.MQMessage;

import java.util.List;

public class MQConsumerPullResponse extends MQCommonResponse {

    private List<MQMessage> list;

    public List<MQMessage> getList() {
        return list;
    }

    public void setList(List<MQMessage> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "MQConsumerPullResponse{" +
                "list=" + list +
                '}';
    }
}
