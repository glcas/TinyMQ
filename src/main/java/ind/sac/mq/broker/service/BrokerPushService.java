package ind.sac.mq.broker.service;

import ind.sac.mq.broker.model.bo.BrokerPushContext;

public interface BrokerPushService {

    void asyncPush(final BrokerPushContext ctx);
}
