package ind.sac.mq.broker.dto.persist;

import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.common.rpc.RPCAddress;

public class MQPersistPutMsg {

    private MQMessage mqMessage;

    private RPCAddress rpcAddress;

    private String msgStatus;

    public MQMessage getMqRequestMessage() {
        return mqMessage;
    }

    public void setMqRequestMessage(MQMessage mqMessage) {
        this.mqMessage = mqMessage;
    }

    public RPCAddress getRpcAddress() {
        return rpcAddress;
    }

    public void setRpcAddress(RPCAddress rpcAddress) {
        this.rpcAddress = rpcAddress;
    }

    public String getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(String msgStatus) {
        this.msgStatus = msgStatus;
    }
}
