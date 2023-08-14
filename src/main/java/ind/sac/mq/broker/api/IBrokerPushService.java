package ind.sac.mq.broker.api;

import ind.sac.mq.broker.support.push.BrokerPushContext;

public interface IBrokerPushService {

    void asyncPush(final BrokerPushContext ctx);
}
