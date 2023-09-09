package ind.sac.mq.consumer.dto.response;

import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.response.CommonResponse;

import java.util.List;

public class ConsumerPullResponse extends CommonResponse {

    private List<Message> messages;

    public ConsumerPullResponse() {
    }

    public ConsumerPullResponse(String responseCode, String responseMessage, List<Message> messages) {
        super(responseCode, responseMessage);
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "ConsumerPullResponse{" +
                "list=" + messages +
                '}';
    }
}
