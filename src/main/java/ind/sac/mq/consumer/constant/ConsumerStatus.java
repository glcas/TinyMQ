package ind.sac.mq.consumer.constant;

public enum ConsumerStatus {

    SUCCESS("SUCCESS", "successfully consumed"),
    FAILED("FAILED", "consume fail"),
    LATER("LATER", "consume later");

    private final String code;
    private final String description;

    ConsumerStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
