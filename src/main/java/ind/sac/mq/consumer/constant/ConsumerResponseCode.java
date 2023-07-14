package ind.sac.mq.consumer.constant;

import ind.sac.mq.common.exception.ResponseCode;

public enum ConsumerResponseCode implements ResponseCode {

    CONSUMER_INIT_FAILED("C01", "consumer start failed");

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
