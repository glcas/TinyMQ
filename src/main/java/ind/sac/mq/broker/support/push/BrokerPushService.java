package ind.sac.mq.broker.support.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.api.IBrokerPushService;
import ind.sac.mq.broker.api.IMQBrokerPersistService;
import ind.sac.mq.broker.support.GroupNameChannel;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.MQMessage;
import ind.sac.mq.common.dto.response.MQConsumeResultResponse;
import ind.sac.mq.common.response.ConsumeStatus;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.utils.JsonUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrokerPushService implements IBrokerPushService {

    private static final Logger logger = LoggerFactory.getLogger(BrokerPushService.class);

    // broker起到核心中转的作用，所以这里静态修饰以保证程序全局线程顺序执行
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void asyncPush(BrokerPushContext ctx) {
        executorService.submit(() -> {
            try {
                logger.info("Start to asynchronously push process: {}", JsonUtil.writeAsJsonString(ctx.mqPersistPutMsg().getMqRequestMessage()));
                final IMQBrokerPersistService mqBrokerPersist = ctx.mqBrokerPersist();
                final MQMessage message = ctx.mqPersistPutMsg().getMqRequestMessage();
                message.setMethodType(MethodType.BROKER_MSG_PUSH);
                final long msgId = message.getTraceId();
                for (GroupNameChannel groupNameChannel : ctx.channelList()) {
                    try {
                        Channel channel = groupNameChannel.getChannel();
                        MQConsumeResultResponse resultResponse = IInvokeService.callServer(channel, message, MQConsumeResultResponse.class, ctx.invokeService(), ctx.responseTimeoutMs());
                        mqBrokerPersist.updateStatus(msgId, Objects.requireNonNull(resultResponse).getConsumeStatus());
                    } catch (Exception e) {
                        logger.error("Push message error!");
                        mqBrokerPersist.updateStatus(msgId, ConsumeStatus.FAIL.getCode());
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
