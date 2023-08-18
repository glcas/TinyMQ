package ind.sac.mq.consumer.support.listener;

import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.common.response.ConsumeStatus;
import ind.sac.mq.consumer.api.IMQConsumerListener;
import ind.sac.mq.consumer.api.IMQConsumerListenerContext;
import ind.sac.mq.consumer.api.IMQConsumerListenerService;

public class MQConsumerListenerService implements IMQConsumerListenerService {

    private IMQConsumerListener listener;

    @Override
    public void register(IMQConsumerListener listener) {
        this.listener = listener;
    }

    @Override
    public ConsumeStatus consume(MQMessage message, IMQConsumerListenerContext context) {
        if (this.listener == null) {
            // 当前消费者没有监听者，没人消费它的消息
            return ConsumeStatus.SUCCESS;
        } else {
            return listener.consume(message, context);
        }
    }
}
