package ind.sac.mq.producer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.response.CommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.CommonResponseCode;
import ind.sac.mq.common.rpc.RPCAddress;
import ind.sac.mq.common.service.invoke.InvokeService;
import ind.sac.mq.common.service.invoke.impl.InvokeServiceImpl;
import ind.sac.mq.common.service.shutdown.Destroyable;
import ind.sac.mq.common.service.shutdown.ShutdownHook;
import ind.sac.mq.common.service.shutdown.impl.ClientShutdownHook;
import ind.sac.mq.common.util.AddressUtil;
import ind.sac.mq.common.util.DelimiterUtil;
import ind.sac.mq.common.util.RandomUtil;
import ind.sac.mq.common.util.SnowFlake;
import ind.sac.mq.producer.constant.ProducerConst;
import ind.sac.mq.producer.constant.ProducerResponseCode;
import ind.sac.mq.producer.constant.SendStatus;
import ind.sac.mq.producer.dto.SendResult;
import ind.sac.mq.producer.handler.ProducerHandler;
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

public class MQProducer extends Thread implements Destroyable {

    private static final Logger logger = LoggerFactory.getLogger(MQProducer.class);

    private final String groupName;

    private int weight = 0;

    private String brokerAddress;

    private final SnowFlake snowFlake;

    private final InvokeService invokeService = new InvokeServiceImpl();

    private final List<Channel> channels = new ArrayList<>();

    // 单位：毫秒
    private long responseTimeout = 5000;

    private long waitTimeForRemainRequest = 60 * 1000;

    private boolean enable = false;

    private String delimiter = DelimiterUtil.DELIMITER;

    private final EventLoopGroup group = new NioEventLoopGroup();

    public MQProducer(String groupName, String brokerAddress, int datacenterId, int machineId) {
        this.groupName = groupName;
        this.brokerAddress = brokerAddress;
        this.snowFlake = new SnowFlake(datacenterId, machineId);
    }

    public MQProducer(String groupName) {
        this(groupName, ProducerConst.DEFAULT_BROKER_ADDRESS, ProducerConst.DEFAULT_DATACENTER_ID, ProducerConst.DEFAULT_MACHINE_ID);
    }

    public MQProducer() {
        this(ProducerConst.DEFAULT_GROUP_NAME, ProducerConst.DEFAULT_BROKER_ADDRESS, ProducerConst.DEFAULT_DATACENTER_ID, ProducerConst.DEFAULT_MACHINE_ID);
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public void setWaitTimeForRemainRequest(long waitTimeForRemainRequest) {
        this.waitTimeForRemainRequest = waitTimeForRemainRequest;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public boolean enable() {
        return enable;
    }

    @Override
    public void setEnableStatus(boolean status) {
        this.enable = status;
    }

    @Override
    public void run() {
        logger.info("Message queue producer start to run; groupName: {}, brokerAddress: {}", groupName, brokerAddress);
        try {
            final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf((this.delimiter));
            List<RPCAddress> addressList = AddressUtil.splitAddrFromStr(brokerAddress);
            for (RPCAddress address :
                    addressList) {
                final String host = address.getHost();
                final int port = address.getPort();
                final int brokerWeight = address.getWeight();
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
                                        .addLast(new ProducerHandler(invokeService));
                            }
                        })
                        .connect(host, port)
                        .syncUninterruptibly();
                logger.info("Successfully created channel between producer and broker {}:{}.", host, port);
                RegisterRequest registerRequest = new RegisterRequest(snowFlake.nextId(), MethodType.PRODUCER_REGISTER, this.groupName, this.weight);
                CommonResponse response = InvokeService.callServer(channelFuture.channel(), registerRequest, CommonResponse.class, invokeService, responseTimeout);
                logger.info("Registration sent to broker {}:{}, got response {}.", host, port, response);
                channels.add(channelFuture.channel());
            }
            final ShutdownHook shutdownHook = new ClientShutdownHook(invokeService, this, waitTimeForRemainRequest);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    shutdownHook.shutdown();
                } catch (Exception e) {
                    throw new MQException(ProducerResponseCode.PRODUCER_SHUTDOWN_ERROR);
                }
            }));
            this.setEnableStatus(true);
            logger.info("Message queue producer server started.");
        } catch (Exception e) {
            logger.error("Error in starting message queue producer");
            throw new MQException(ProducerResponseCode.PRODUCER_INIT_FAILED);
        }
    }

    public SendResult syncSend(Message message) throws JsonProcessingException, InterruptedException {
        // 确保producer可用（成功启动）才执行消息发送功能
        while (!this.enable()) {
            Thread.sleep(10);
        }

        long traceId = this.snowFlake.nextId();
        message.setTraceId(traceId);
        message.setMethodType(MethodType.PRODUCER_SEND_MSG);
        Channel channel = Objects.requireNonNull(RandomUtil.loadBalance(channels, message.getShardingKey()));
        CommonResponse response = Objects.requireNonNull(InvokeService.callServer(channel, message, CommonResponse.class, invokeService, responseTimeout));
        if (CommonResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return SendResult.of(traceId, SendStatus.SUCCESS);
        } else {
            return SendResult.of(traceId, SendStatus.FAIL);
        }
    }

    /**
     * 发了就视为成功，不管对面收没收到
     *
     * @param message MQ message
     * @return Success signal
     */
    public SendResult onewaySend(Message message) throws JsonProcessingException, InterruptedException {
        while (!this.enable()) {
            Thread.sleep(10);
        }
        long traceId = this.snowFlake.nextId();
        message.setTraceId(traceId);
        message.setMethodType(MethodType.PRODUCER_SEND_MSG);
        Channel channel = Objects.requireNonNull(RandomUtil.loadBalance(channels, message.getShardingKey()));
        InvokeService.callServer(channel, message, null, invokeService, responseTimeout);
        return SendResult.of(traceId, SendStatus.SUCCESS);
    }

    @Override
    public void destroy() throws JsonProcessingException {
        for (Channel channel :
                channels) {
            // 从broker注销
            RegisterRequest unregisterReq = new RegisterRequest(snowFlake.nextId(), MethodType.PRODUCER_UNREGISTER, this.groupName, this.weight);
            InvokeService.callServer(channel, unregisterReq, null, invokeService, responseTimeout);
        }
        group.shutdownGracefully();
    }
}
