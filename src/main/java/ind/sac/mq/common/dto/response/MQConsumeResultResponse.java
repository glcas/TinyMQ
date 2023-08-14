package ind.sac.mq.common.dto.response;

public class MQConsumeResultResponse extends MQCommonResponse {

    private String consumeStatus;

    public MQConsumeResultResponse() {
    }

    public MQConsumeResultResponse(String mqCommonResponseCode, String mqCommonResponseMsg) {
        super(mqCommonResponseCode, mqCommonResponseMsg);
    }

    public MQConsumeResultResponse(String mqCommonResponseCode, String mqCommonResponseMsg, String consumeStatus) {
        super(mqCommonResponseCode, mqCommonResponseMsg);
        this.consumeStatus = consumeStatus;
    }

    public String getConsumeStatus() {
        return consumeStatus;
    }

    public void setConsumeStatus(String consumeStatus) {
        this.consumeStatus = consumeStatus;
    }
}
