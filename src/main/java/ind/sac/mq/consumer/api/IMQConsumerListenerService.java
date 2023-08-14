package ind.sac.mq.consumer.api;

import ind.sac.mq.common.dto.request.MQRequestMessage;
import ind.sac.mq.common.response.ConsumeStatus;

public interface IMQConsumerListenerService {

    void register(final IMQConsumerListener listener);

    ConsumeStatus consume(final MQRequestMessage message, final IMQConsumerListenerContext context);
}
