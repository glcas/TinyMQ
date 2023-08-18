package ind.sac.mq.producer.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.producer.dto.SendResult;

public interface IMQProducer {

    SendResult syncSend(final MQMessage mqMessage) throws JsonProcessingException;

    /**
     * 发了就视为成功，不管对面收没收到
     *
     * @param mqMessage MQ message
     * @return success signal
     * @throws JsonProcessingException json process exception
     */
    SendResult onewaySend(final MQMessage mqMessage) throws JsonProcessingException;

}
