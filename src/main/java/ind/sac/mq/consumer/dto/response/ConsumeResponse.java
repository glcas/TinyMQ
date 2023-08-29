package ind.sac.mq.consumer.dto.response;

import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.consumer.constant.ConsumeStatus;

public class ConsumeResponse extends CommonResponse {

    private ConsumeStatus consumeStatus;

    public ConsumeResponse() {
    }

    public ConsumeResponse(String mqCommonResponseCode, String mqCommonResponseMsg) {
        super(mqCommonResponseCode, mqCommonResponseMsg);
    }

    public ConsumeResponse(String mqCommonResponseCode, String mqCommonResponseMsg, ConsumeStatus consumeStatus) {
        super(mqCommonResponseCode, mqCommonResponseMsg);
        this.consumeStatus = consumeStatus;
    }

    public ConsumeStatus getConsumeStatus() {
        return consumeStatus;
    }

    public void setConsumeStatus(ConsumeStatus consumeStatus) {
        this.consumeStatus = consumeStatus;
    }
}
