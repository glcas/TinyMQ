package ind.sac.mq.common.constant;

/**
 * 请求方法类型枚举
 */
public enum MethodType {
    PRODUCER_SEND_MSG("PRODUCER_SEND_MESSAGE"),

    PRODUCER_SEND_MSG_ONE_WAY("PRODUCER_SEND_MESSAGE_ONE_WAY"),

    PRODUCER_REGISTER("PRODUCER_REGISTER"),

    PRODUCER_UNREGISTER("PRODUCER_UNREGISTER"),

    CONSUMER_REGISTER("CONSUMER_REGISTER"),

    CONSUMER_UNREGISTER("CONSUMER_UNREGISTER"),

    CONSUMER_SUB("CONSUMER_SUBSCRIBE"),

    CONSUMER_UNSUB("CONSUMER_UNSUBSCRIBE"),

    CONSUMER_HEARTBEAT("CONSUMER_HEARTBEAT"),

    CONSUMER_MSG_PULL("CONSUMER_MESSAGE_PULL"),

    BROKER_MSG_PUSH("BROKER_MESSAGE_PUSH");

    private final String value;

    MethodType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
