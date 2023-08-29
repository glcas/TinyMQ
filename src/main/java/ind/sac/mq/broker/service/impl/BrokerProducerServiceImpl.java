package ind.sac.mq.broker.service.impl;

import ind.sac.mq.broker.constant.BrokerResponseCode;
import ind.sac.mq.broker.model.bo.ServiceEntry;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.broker.service.BrokerProducerService;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.CommonResponseCode;
import ind.sac.mq.producer.constant.ProducerResponseCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrokerProducerServiceImpl implements BrokerProducerService {

    private static final Logger logger = LoggerFactory.getLogger(BrokerProducerServiceImpl.class);

    // key: channel id
    private final Map<String, ServiceEntry> registerMap = new ConcurrentHashMap<>();

    @Override
    public CommonResponse register(RegisterRequest registerRequest, Channel channel) {
        final String channelId = channel.id().asLongText();
        ServiceEntry serviceEntry = new ServiceEntry(registerRequest.getGroupName(), channel, registerRequest.getWeight());
        serviceEntry.setLastAccessTime(System.currentTimeMillis());
        registerMap.put(channelId, serviceEntry);
        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public CommonResponse unregister(RegisterRequest registerRequest, Channel channel) {
        registerMap.remove(channel.id().asLongText());
        logger.info("Register info removed - channel id: {}", channel.id().asLongText());
        channel.closeFuture().addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                throw new MQException(future.cause(), ProducerResponseCode.PRODUCER_SHUTDOWN_ERROR);
            }
        });
        channel.close();

        return new CommonResponse(CommonResponseCode.SUCCESS.getCode(), CommonResponseCode.SUCCESS.getMessage());
    }

    @Override
    public ServiceEntry getServiceEntry(String channelId) {
        return registerMap.get(channelId);
    }

    @Override
    public void checkChannelValid(String channelId) {
        if (!registerMap.containsKey(channelId)) {
            logger.error("Channel ID: {} isn't registered.", channelId);
            throw new MQException(BrokerResponseCode.PRODUCER_REGISTER_CHANNEL_NOT_VALID);
        }
    }
}
