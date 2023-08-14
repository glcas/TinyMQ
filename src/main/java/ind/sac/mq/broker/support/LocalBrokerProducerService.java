package ind.sac.mq.broker.support;

import ind.sac.mq.broker.api.IBrokerProducerService;
import ind.sac.mq.broker.constant.MQBrokerResponseCode;
import ind.sac.mq.broker.utils.ChannelUtils;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.MQCommonResponseCode;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalBrokerProducerService implements IBrokerProducerService {

    private static final Logger logger = LoggerFactory.getLogger(LocalBrokerProducerService.class);

    // key: channel id
    private final Map<String, BrokerServiceEntryChannel> registerMap = new ConcurrentHashMap<>();

    @Override
    public MQCommonResponse register(ServiceEntry serviceEntry, Channel channel) {
        final String channelId = channel.id().asLongText();
        BrokerServiceEntryChannel entryChannel = ChannelUtils.buildEntryChannel(serviceEntry, channel);
        registerMap.put(channelId, entryChannel);

        return new MQCommonResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription());
    }

    @Override
    public MQCommonResponse unRegister(ServiceEntry serviceEntry, Channel channel) {
        final String channelId = channel.id().asLongText();
        registerMap.remove(channelId);

        return new MQCommonResponse(MQCommonResponseCode.SUCCESS.getCode(), MQCommonResponseCode.SUCCESS.getDescription());
    }

    /**
     * Get info of service address.
     *
     * @param channelId String of ID of netty channel
     * @return Message common queue response including specific info
     */
    @Override
    public ServiceEntry getServiceEntry(String channelId) {
        return registerMap.get(channelId);
    }

    @Override
    public void checkChannelValid(String channelId) {
        if (!registerMap.containsKey(channelId)) {
            logger.error("Channel ID: {} isn't registered.", channelId);
            throw new MQException(MQBrokerResponseCode.PRODUCER_REGISTER_CHANNEL_NOT_VALID);
        }
    }
}
