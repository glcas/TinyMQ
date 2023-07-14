package ind.sac.mq.common.exception;

public class MqException extends RuntimeException {

    private final ResponseCode responseCode;

    public MqException(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public MqException(String message, ResponseCode responseCode) {
        super(message);
        this.responseCode = responseCode;
    }

    public MqException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public MqException(Throwable cause, ResponseCode responseCode) {
        super(cause);
        this.responseCode = responseCode;
    }

    public MqException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ResponseCode responseCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.responseCode = responseCode;
    }

}
