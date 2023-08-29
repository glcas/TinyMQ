package ind.sac.mq.consumer.service.listener.impl;

import ind.sac.mq.common.dto.Message;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import ind.sac.mq.consumer.service.listener.ConsumerListener;
import ind.sac.mq.consumer.service.listener.ConsumerListenerContext;
import ind.sac.mq.consumer.service.listener.ConsumerListenerService;

public class ConsumerListenerServiceImpl implements ConsumerListenerService {

    private ConsumerListener listener;

    @Override
    public void register(ConsumerListener listener) {
        this.listener = listener;
    }

    @Override
    public ConsumeStatus consume(Message message, ConsumerListenerContext context) {
        if (this.listener == null) {
            // 当前消费者没有监听者，没人消费它的消息
            return ConsumeStatus.SUCCESS;
        } else {
            return listener.consume(message, context);
        }
    }
}
