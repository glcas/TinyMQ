package ind.sac.mq.consumer.service.listener;

import ind.sac.mq.common.dto.Message;
import ind.sac.mq.consumer.constant.ConsumeStatus;

public interface ConsumerListenerService {

    void register(final ConsumerListener listener);

    ConsumeStatus consume(final Message message, final ConsumerListenerContext context);
}
