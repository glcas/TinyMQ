package ind.sac.mq.common.exception;

public enum MQCommonResponseCode implements ResponseCode {

    SUCCESS("0", "success"),
    TIMEOUT("-1", "timeout"),
    FAIL("-2", "fail");

    private final String code;
    private final String description;

    MQCommonResponseCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
