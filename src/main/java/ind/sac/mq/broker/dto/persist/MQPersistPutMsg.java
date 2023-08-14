package ind.sac.mq.broker.dto.persist;

import ind.sac.mq.common.dto.request.MQRequestMessage;
import ind.sac.mq.common.rpc.RPCAddress;

public class MQPersistPutMsg {

    private MQRequestMessage mqRequestMessage;

    private RPCAddress rpcAddress;

    private String msgStatus;

    public MQRequestMessage getMqRequestMessage() {
        return mqRequestMessage;
    }

    public void setMqRequestMessage(MQRequestMessage mqRequestMessage) {
        this.mqRequestMessage = mqRequestMessage;
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
