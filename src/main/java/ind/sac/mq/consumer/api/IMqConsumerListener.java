package ind.sac.mq.consumer.api;

import ind.sac.mq.common.dto.MqMessage;
import ind.sac.mq.consumer.constant.ConsumerStatus;

public interface IMqConsumerListener {

    ConsumerStatus consumer(final MqMessage mqMessage, final IMqConsumerListenerContext context);

}
