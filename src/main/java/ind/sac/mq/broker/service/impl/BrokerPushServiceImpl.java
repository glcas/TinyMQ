package ind.sac.mq.broker.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.model.bo.BrokerPushContext;
import ind.sac.mq.broker.model.bo.GroupNameChannel;
import ind.sac.mq.broker.service.BrokerPersistenceService;
import ind.sac.mq.broker.service.BrokerPushService;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.service.invoke.InvokeService;
import ind.sac.mq.common.util.JsonUtil;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import ind.sac.mq.consumer.dto.response.ConsumeResponse;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrokerPushServiceImpl implements BrokerPushService {

    private static final Logger logger = LoggerFactory.getLogger(BrokerPushServiceImpl.class);

    // broker起到核心中转的作用，所以这里静态修饰以保证程序全局线程顺序执行
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void asyncPush(BrokerPushContext ctx) {
        executorService.submit(() -> {
            try {
                final Message message = ctx.getMessagePO().getMessage();
                logger.info("Start to asynchronously push process: {}", JsonUtil.writeAsJsonString(message));
                final BrokerPersistenceService persistenceService = ctx.getPersistenceService();
                message.setMethodType(MethodType.BROKER_MSG_PUSH);
                final long msgId = message.getTraceId();
                for (GroupNameChannel groupNameChannel : ctx.channelList()) {
                    try {
                        Channel channel = groupNameChannel.getChannel();
                        ConsumeResponse resultResponse = InvokeService.callServer(channel, message, ConsumeResponse.class, ctx.invokeService(), ctx.responseTimeout());
                        persistenceService.updateStatus(msgId, Objects.requireNonNull(resultResponse).getConsumeStatus());
                    } catch (Exception e) {
                        logger.error("Push message error!");
                        persistenceService.updateStatus(msgId, ConsumeStatus.FAIL);
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
