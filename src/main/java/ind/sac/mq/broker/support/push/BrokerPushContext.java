package ind.sac.mq.broker.support.push;

import ind.sac.mq.broker.api.IMQBrokerPersistService;
import ind.sac.mq.broker.dto.persist.MQPersistPutMsg;
import ind.sac.mq.broker.support.GroupNameChannel;
import ind.sac.mq.common.support.invoke.IInvokeService;

import java.util.List;
import java.util.Map;

public class BrokerPushContext {

    private IMQBrokerPersistService mqBrokerPersist;

    private MQPersistPutMsg mqPersistPutMsg;

    private List<GroupNameChannel> channelList;

    private IInvokeService invokeService;

    private long responseTimeoutMs;

    private int maxPushAttemptTimes;

    /**
     * 意义不明？
     */
    private Map<String, String> channelGroupMap;

    public static BrokerPushContext newInstance() {
        return new BrokerPushContext();
    }

    public IMQBrokerPersistService mqBrokerPersist() {
        return mqBrokerPersist;
    }

    public BrokerPushContext setMQBrokerPersist(IMQBrokerPersistService mqBrokerPersist) {
        this.mqBrokerPersist = mqBrokerPersist;
        return this;
    }

    public MQPersistPutMsg mqPersistPutMsg() {
        return mqPersistPutMsg;
    }

    public BrokerPushContext setMQPersistPutMsg(MQPersistPutMsg mqPersistPutMsg) {
        this.mqPersistPutMsg = mqPersistPutMsg;
        return this;
    }

    public List<GroupNameChannel> channelList() {
        return channelList;
    }

    public BrokerPushContext setChannelList(List<GroupNameChannel> channelList) {
        this.channelList = channelList;
        return this;
    }

    public IInvokeService invokeService() {
        return invokeService;
    }

    public BrokerPushContext setInvokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
        return this;
    }


    public long responseTimeoutMs() {
        return responseTimeoutMs;
    }

    public BrokerPushContext setResponseTimeoutMs(long responseTimeoutMs) {
        this.responseTimeoutMs = responseTimeoutMs;
        return this;
    }

    public int maxPushAttemptTimes() {
        return maxPushAttemptTimes;
    }

    public BrokerPushContext setMaxPushAttemptTimes(int maxPushAttemptTimes) {
        this.maxPushAttemptTimes = maxPushAttemptTimes;
        return this;
    }

    public Map<String, String> channelGroupMap() {
        return channelGroupMap;
    }

    public BrokerPushContext channelGroupMap(Map<String, String> channelGroupMap) {
        this.channelGroupMap = channelGroupMap;
        return this;
    }
}
