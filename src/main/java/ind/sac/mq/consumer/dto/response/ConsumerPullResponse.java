package ind.sac.mq.consumer.dto.response;

import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.response.CommonResponse;

import java.util.List;

public class ConsumerPullResponse extends CommonResponse {

    private List<Message> list;

    public List<Message> getList() {
        return list;
    }

    public void setList(List<Message> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "ConsumerPullResponse{" +
                "list=" + list +
                '}';
    }
}
