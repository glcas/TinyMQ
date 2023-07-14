package ind.sac.mq.producer.api;

import ind.sac.mq.common.dto.MqMessage;
import ind.sac.mq.producer.dto.SendResult;

public interface IMqProducer {

    SendResult syncSend(final MqMessage mqMessage);

    SendResult onewaySend(final MqMessage mqMessage);

}
