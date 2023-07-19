package ind.sac.mq.common.support.invoke;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.rpc.RPCMessageDTO;

public interface IInvokeService {

    /**
     * add request
     *
     * @param sequenceId sequence ID
     * @param timeout    timeout milliseconds
     */
    void addRequest(final String sequenceId, final long timeout);

    void addResponse(final String sequenceId, final RPCMessageDTO rpcResponse) throws JsonProcessingException;

    RPCMessageDTO getResponse(final String sequenceId);
}
