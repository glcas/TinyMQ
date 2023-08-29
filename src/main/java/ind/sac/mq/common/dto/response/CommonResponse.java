package ind.sac.mq.common.dto.response;

import java.io.Serializable;
import java.util.Objects;

public class CommonResponse implements Serializable {
    private String responseCode;
    private String responseMessage;

    public CommonResponse() {
    }

    public CommonResponse(String responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CommonResponse other)) return false;
        if (!other.canEqual(this)) return false;
        final Object this$responseCode = this.getResponseCode();
        final Object other$responseCode = other.getResponseCode();
        if (!Objects.equals(this$responseCode, other$responseCode))
            return false;
        final Object this$responseMessage = this.getResponseMessage();
        final Object other$responseMessage = other.getResponseMessage();
        return Objects.equals(this$responseMessage, other$responseMessage);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CommonResponse;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $responseCode = this.getResponseCode();
        result = result * PRIME + ($responseCode == null ? 43 : $responseCode.hashCode());
        final Object $responseMessage = this.getResponseMessage();
        result = result * PRIME + ($responseMessage == null ? 43 : $responseMessage.hashCode());
        return result;
    }

    public String toString() {
        return "CommonResponse(responseCode=" + this.getResponseCode() + ", responseMessage=" + this.getResponseMessage() + ")";
    }
}
