package ind.sac.mq.producer.constant;

public enum SendStatus {

    SUCCESS("SUCCESS", "successfully send"),
    FAIL("FAIL", "send failed");

    private final String code;
    private final String description;

    SendStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
