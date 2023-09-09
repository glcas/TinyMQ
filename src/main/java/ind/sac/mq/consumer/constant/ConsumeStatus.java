package ind.sac.mq.consumer.constant;

public enum ConsumeStatus {
    IDLE("IDLE", "Wait for consumer."),
    PROCESSING("PROCESSING", "Broker and consumer are processing the message."),
    SUCCESS("SUCCESS", "Successfully consumed."),
    FAIL("FAIL", "Consume failed."),
    LATER("LATER", "Consume later.");
    private final String code;
    private final String description;

    ConsumeStatus(String code, String description) {
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
