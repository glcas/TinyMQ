package ind.sac.mq.producer.constant;

import ind.sac.mq.common.exception.ResponseCode;

public enum ProducerResponseCode implements ResponseCode {

    PRODUCER_INIT_FAILED("P01", "producer start error");

    private final String code;
    private final String description;

    ProducerResponseCode(String code, String description) {
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
