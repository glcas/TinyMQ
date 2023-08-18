package ind.sac.mq.consumer.api;

import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.common.response.ConsumeStatus;

public interface IMQConsumerListenerService {

    void register(final IMQConsumerListener listener);

    ConsumeStatus consume(final MQMessage message, final IMQConsumerListenerContext context);
}
