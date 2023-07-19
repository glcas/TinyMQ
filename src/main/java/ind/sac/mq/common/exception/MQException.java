package ind.sac.mq.common.exception;

public class MQException extends RuntimeException {

    private final ResponseCode responseCode;

    public MQException(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public MQException(String message, ResponseCode responseCode) {
        super(message);
        this.responseCode = responseCode;
    }

    public MQException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public MQException(Throwable cause, ResponseCode responseCode) {
        super(cause);
        this.responseCode = responseCode;
    }

    public MQException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ResponseCode responseCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.responseCode = responseCode;
    }

}
