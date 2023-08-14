package ind.sac.mq.producer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.dto.BrokerRegisterRequest;
import ind.sac.mq.broker.support.ServiceEntry;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.MQRequestMessage;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.MQCommonResponseCode;
import ind.sac.mq.common.rpc.RPCAddress;
import ind.sac.mq.common.rpc.RPCChannelFuture;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.support.invoke.impl.InvokeService;
import ind.sac.mq.common.utils.AddressUtil;
import ind.sac.mq.common.utils.DelimiterUtil;
import ind.sac.mq.common.utils.RandomUtil;
import ind.sac.mq.common.utils.SnowFlake;
import ind.sac.mq.producer.api.IMQProducer;
import ind.sac.mq.producer.constant.ProducerConst;
import ind.sac.mq.producer.constant.ProducerResponseCode;
import ind.sac.mq.producer.constant.SendStatus;
import ind.sac.mq.producer.dto.SendResult;
import ind.sac.mq.producer.handler.MQProducerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MQProducer extends Thread implements IMQProducer {

    private static final Logger logger = LoggerFactory.getLogger(MQProducer.class);

    private final String groupName;

    private final SnowFlake snowFlake;

    private final IInvokeService invokeService = new InvokeService();

    private final List<RPCChannelFuture> channelFutureList = new ArrayList<>();

    private long responseTimeoutMilliseconds = 5000;

    private boolean enable = false;
    private String brokerAddr;

    private String delimiter = DelimiterUtil.DELIMITER;

    public MQProducer(String groupName, String brokerAddr, int datacenterId, int machineId) {
        this.groupName = groupName;
        this.brokerAddr = brokerAddr;
        this.snowFlake = new SnowFlake(datacenterId, machineId);
    }

    public MQProducer(String groupName) {
        this(groupName, ProducerConst.DEFAULT_BROKER_ADDRESS, ProducerConst.DEFAULT_DATACENTER_ID, ProducerConst.DEFAULT_MACHINE_ID);
    }

    public MQProducer() {
        this(ProducerConst.DEFAULT_GROUP_NAME, ProducerConst.DEFAULT_BROKER_ADDRESS, ProducerConst.DEFAULT_DATACENTER_ID, ProducerConst.DEFAULT_MACHINE_ID);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public void setResponseTimeoutMilliseconds(long responseTimeoutMilliseconds) {
        this.responseTimeoutMilliseconds = responseTimeoutMilliseconds;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void run() {
        logger.info("Message queue producer start to run; groupName: {}, brokerAddress: {}", groupName, brokerAddr);
        try {
            final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf((this.delimiter));
            List<RPCAddress> addressList = AddressUtil.splitAddrFromStr(brokerAddr);
            for (RPCAddress address :
                    addressList) {
                final String host = address.getHost();
                final int port = address.getPort();
                final int weight = address.getWeight();
                EventLoopGroup group = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap();
                ChannelFuture channelFuture = bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline()
                                        // .addLast(new LoggingHandler(LogLevel.INFO))
                                        .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                                        .addLast(new MQProducerHandler(invokeService));
                            }
                        })
                        .connect(host, port)
                        .syncUninterruptibly();
                logger.info("Successfully created channel between producer and broker {}:{}.", host, port);
                ServiceEntry serviceEntry = new ServiceEntry(host, port, weight, groupName);
                BrokerRegisterRequest registerRequest = new BrokerRegisterRequest(snowFlake.nextId(), MethodType.PRODUCER_REGISTER, serviceEntry);
                MQCommonResponse response = IInvokeService.callServer(channelFuture.channel(), registerRequest, MQCommonResponse.class, invokeService, responseTimeoutMilliseconds);
                logger.info("Registration sent to broker {}:{}, got response {}.", host, port, response);
                channelFutureList.add(new RPCChannelFuture(host, port, weight, channelFuture));
            }
            enable = true;
            logger.info("Message queue producer server started.");
        } catch (Exception e) {
            logger.error("Error in starting message queue producer");
            throw new MQException(ProducerResponseCode.PRODUCER_INIT_FAILED);
        }
    }

    @Override
    public SendResult syncSend(MQRequestMessage mqMessage) throws JsonProcessingException {
        long messageId = this.snowFlake.nextId();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.PRODUCER_SEND_MSG);
        Channel channel = Objects.requireNonNull(RandomUtil.loadBalance(channelFutureList, mqMessage.getShardingKey())).getChannelFuture().channel();
        MQCommonResponse response = IInvokeService.callServer(channel, mqMessage, MQCommonResponse.class, invokeService, responseTimeoutMilliseconds);
        if (MQCommonResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return SendResult.of(messageId, SendStatus.SUCCESS);
        } else {
            return SendResult.of(messageId, SendStatus.FAIL);
        }
    }

    @Override
    public SendResult onewaySend(MQRequestMessage mqMessage) throws JsonProcessingException {
        long messageId = this.snowFlake.nextId();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.PRODUCER_SEND_MSG);
        Channel channel = Objects.requireNonNull(RandomUtil.loadBalance(channelFutureList, mqMessage.getShardingKey())).getChannelFuture().channel();
        IInvokeService.callServer(channel, mqMessage, null, invokeService, responseTimeoutMilliseconds);
        return SendResult.of(messageId, SendStatus.FAIL);
    }
}
