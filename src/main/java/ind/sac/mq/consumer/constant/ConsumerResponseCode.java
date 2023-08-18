package ind.sac.mq.consumer.constant;

import ind.sac.mq.common.response.ResponseCode;

public enum ConsumerResponseCode implements ResponseCode {

    CONSUMER_INIT_FAILED("C01", "Consumer start failed."),
    CONSUMER_SUB_FAILED("C02", "Consumer subscribe failed."),
    CONSUMER_UNSUB_FAILED("C03", "Consumer unsubscribe failed."),
    CONSUMER_SHUTDOWN_ERROR("P02", "Error occurred while shutdown!");

    private final String code;
    private final String description;

    ConsumerResponseCode(String code, String description) {
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
