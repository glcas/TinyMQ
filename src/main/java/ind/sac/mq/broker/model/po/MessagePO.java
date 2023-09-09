package ind.sac.mq.broker.model.po;

import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.rpc.RPCAddress;
import ind.sac.mq.consumer.constant.ConsumeStatus;

public class MessagePO {

    private Message message;

    private RPCAddress rpcAddress;

    private ConsumeStatus consumeStatus;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public RPCAddress getRpcAddress() {
        return rpcAddress;
    }

    public void setRpcAddress(RPCAddress rpcAddress) {
        this.rpcAddress = rpcAddress;
    }

    public ConsumeStatus getConsumeStatus() {
        return consumeStatus;
    }

    public void setConsumeStatus(ConsumeStatus consumeStatus) {
        this.consumeStatus = consumeStatus;
    }

    @Override
    public String toString() {
        return "MessagePO{" +
                "message=" + message.getTopic() + " - " + message.getTags() + ", " +
                "status=" + consumeStatus +
                '}';
    }
}
