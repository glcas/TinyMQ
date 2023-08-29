package ind.sac.mq.broker.model.bo;

import ind.sac.mq.broker.model.po.MessagePO;
import ind.sac.mq.broker.service.BrokerPersistenceService;
import ind.sac.mq.common.service.invoke.InvokeService;

import java.util.List;

public class BrokerPushContext {

    private BrokerPersistenceService persistenceService;

    private MessagePO messagePO;

    private List<GroupNameChannel> channelList;

    private InvokeService invokeService;

    // time unit: milliseconds
    private long responseTimeout;

    private int maxPushAttemptTimes;

    public static BrokerPushContext newInstance() {
        return new BrokerPushContext();
    }

    public BrokerPersistenceService getPersistenceService() {
        return persistenceService;
    }

    public BrokerPushContext setPersistenceService(BrokerPersistenceService mqBrokerPersist) {
        this.persistenceService = mqBrokerPersist;
        return this;
    }

    public MessagePO getMessagePO() {
        return messagePO;
    }

    public BrokerPushContext setMessagePO(MessagePO messagePO) {
        this.messagePO = messagePO;
        return this;
    }

    public List<GroupNameChannel> channelList() {
        return channelList;
    }

    public BrokerPushContext setChannelList(List<GroupNameChannel> channelList) {
        this.channelList = channelList;
        return this;
    }

    public InvokeService invokeService() {
        return invokeService;
    }

    public BrokerPushContext setInvokeService(InvokeService invokeService) {
        this.invokeService = invokeService;
        return this;
    }


    public long responseTimeout() {
        return responseTimeout;
    }

    public BrokerPushContext setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    public int maxPushAttemptTimes() {
        return maxPushAttemptTimes;
    }

    public BrokerPushContext setMaxPushAttemptTimes(int maxPushAttemptTimes) {
        this.maxPushAttemptTimes = maxPushAttemptTimes;
        return this;
    }
}
