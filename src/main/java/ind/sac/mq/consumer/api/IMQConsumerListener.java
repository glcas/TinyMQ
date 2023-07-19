package ind.sac.mq.consumer.api;

import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.consumer.constant.ConsumerStatus;

public interface IMQConsumerListener {

    ConsumerStatus consumer(final MQMessage mqMessage, final IMQConsumerListenerContext context);

}
