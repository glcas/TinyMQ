package ind.sac.mq.consumer.api;

import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.common.response.ConsumeStatus;

/**
 * 消费者监听者接口，具体业务处理（消费）消费者收到的消息
 */
public interface IMQConsumerListener {

    ConsumeStatus consume(final MQMessage message, final IMQConsumerListenerContext context);

}
