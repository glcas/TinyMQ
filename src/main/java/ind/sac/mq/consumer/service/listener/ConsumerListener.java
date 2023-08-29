package ind.sac.mq.consumer.service.listener;

import ind.sac.mq.common.dto.Message;
import ind.sac.mq.consumer.constant.ConsumeStatus;

/**
 * 消费者监听者接口，具体业务处理（消费）消费者收到的消息
 */
public interface ConsumerListener {

    ConsumeStatus consume(final Message message, final ConsumerListenerContext context);

}
