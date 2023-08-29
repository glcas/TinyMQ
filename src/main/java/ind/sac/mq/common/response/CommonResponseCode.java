package ind.sac.mq.common.response;

public enum CommonResponseCode implements ResponseCode {

    SUCCESS("0", "success"),
    TIMEOUT("-1", "timeout"),
    FAIL("-2", "fail");

    private final String code;
    private final String message;

    CommonResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
