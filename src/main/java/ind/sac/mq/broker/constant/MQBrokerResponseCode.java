package ind.sac.mq.broker.constant;

import ind.sac.mq.common.response.ResponseCode;

public enum MQBrokerResponseCode implements ResponseCode {

    BROKER_INIT_FAILED("B01", "Broker start failed."),
    BROKER_NOT_SUPPORT_METHOD("B02", "Method temporarily not supported."),

    PRODUCER_REGISTER_VALID_FAILED("BP01", "Producer registration validation check failed"),
    PRODUCER_REGISTER_CHANNEL_NOT_VALID("BP02", "Producer channel not valid"),

    CONSUMER_REGISTER_VALID_FAILED("CP01", "Consumer registration validation check failed"),
    CONSUMER_REGISTER_CHANNEL_NOT_VALID("CP02", "Consumer channel not valid"),
    ;

    private final String code;
    private final String description;

    MQBrokerResponseCode(String code, String description) {
        this.code = code;
        this.description = description;
    }


    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
