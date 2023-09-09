package ind.sac.mq.consumer.dto.request;

import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.CommonRequest;
import ind.sac.mq.consumer.constant.ConsumeStatus;

public class ConsumeStatusUpdateRequest extends CommonRequest {

    private long messageTraceID;

    private ConsumeStatus consumeStatus;

    public ConsumeStatusUpdateRequest() {
    }

    public ConsumeStatusUpdateRequest(long traceId, MethodType methodType, long messageTraceID, ConsumeStatus consumeStatus) {
        super(traceId, methodType);
        this.messageTraceID = messageTraceID;
        this.consumeStatus = consumeStatus;
    }

    public long getMessageTraceID() {
        return messageTraceID;
    }

    public ConsumeStatus getConsumeStatus() {
        return consumeStatus;
    }
}
