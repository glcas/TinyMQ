package ind.sac.mq.consumer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.dto.BrokerRegisterRequest;
import ind.sac.mq.broker.dto.consumer.ConsumerSubscribeRequest;
import ind.sac.mq.broker.support.ServiceEntry;
import ind.sac.mq.broker.utils.ChannelUtils;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.response.MQCommonResponseCode;
import ind.sac.mq.common.rpc.RPCAddress;
import ind.sac.mq.common.rpc.RPCChannelFuture;
import ind.sac.mq.common.support.Destroyable;
import ind.sac.mq.common.support.hook.IShutdownHook;
import ind.sac.mq.common.support.hook.impl.ClientShutdownHook;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.support.invoke.impl.InvokeService;
import ind.sac.mq.common.utils.AddressUtil;
import ind.sac.mq.common.utils.DelimiterUtil;
import ind.sac.mq.common.utils.SnowFlake;
import ind.sac.mq.consumer.api.IMQConsumer;
import ind.sac.mq.consumer.api.IMQConsumerListener;
import ind.sac.mq.consumer.api.IMQConsumerListenerService;
import ind.sac.mq.consumer.constant.ConsumerConst;
import ind.sac.mq.consumer.constant.ConsumerResponseCode;
import ind.sac.mq.consumer.handler.MQConsumerHandler;
import ind.sac.mq.consumer.support.listener.MQConsumerListenerService;
import ind.sac.mq.consumer.support.subscribe.LocalSubscribeInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MQConsumer extends Thread implements IMQConsumer, Destroyable {

    private static final Logger logger = LoggerFactory.getLogger(MQConsumer.class);

    private final String groupName;

    private final SnowFlake snowFlake;

    private final List<RPCChannelFuture> channelFutureList = new ArrayList<>();

    private final IInvokeService invokeService = new InvokeService();

    private final IMQConsumerListenerService listenerService = new MQConsumerListenerService();

    private String brokerAddr;

    private long responseTimeoutMilliseconds = 5000;

    // 本地记录且同步此消费者的订阅信息，以便在消费者注销时批量取消订阅
    private final Set<LocalSubscribeInfo> localSubscribeInfoSet = new HashSet<>();

    // channel线程池
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private String delimiter = DelimiterUtil.DELIMITER;

    private boolean enable = false;
    private long waitTimeForRemainRequest = 60 * 1000;

    public MQConsumer(String groupName, String brokerAddr, int datacenterId, int machineId) {
        this.groupName = groupName;
        this.brokerAddr = brokerAddr;
        this.snowFlake = new SnowFlake(datacenterId, machineId);
    }

    public MQConsumer(String groupName) {
        this(groupName, ConsumerConst.DEFAULT_BROKER_ADDRESS, ConsumerConst.DEFAULT_DATACENTER_ID, ConsumerConst.DEFAULT_MACHINE_ID);
    }

    public MQConsumer() {
        this(ConsumerConst.DEFAULT_GROUP_NAME, ConsumerConst.DEFAULT_BROKER_ADDRESS, ConsumerConst.DEFAULT_DATACENTER_ID, ConsumerConst.DEFAULT_MACHINE_ID);
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public void setResponseTimeoutMilliseconds(long responseTimeoutMilliseconds) {
        this.responseTimeoutMilliseconds = responseTimeoutMilliseconds;
    }

    public void setWaitTimeForRemainRequest(long waitTimeForRemainRequest) {
        this.waitTimeForRemainRequest = waitTimeForRemainRequest;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean enable() {
        return enable;
    }

    @Override
    public void setEnableStatus(boolean status) {
        this.enable = status;
    }

    @Override
    public void run() {
        logger.info("Message queue consumer start to run; groupName: {}, brokerAddress: {}", groupName, brokerAddr);
        try {
            final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf((this.delimiter));

            // 遍历建立消费者与其关联的那些brokers之间的channel，并且在对应broker储存本consumer的信息（注册）
            // 在RocketMQ中，消费者/生产者通过请求NameServer集群中的一个，来获得其所需的消息路由信息(brokers)
            // 这里目前没有实现路由匹配功能，所以消费者/生产者一方需要尽量多与broker建立通道，本项目选择消费者与所有broker建立通道
            List<RPCAddress> addressList = AddressUtil.splitAddrFromStr(brokerAddr);
            for (RPCAddress address :
                    addressList) {
                final String host = address.getHost();
                final int port = address.getPort();
                final int weight = address.getWeight();

                // 消费者作为客户端，建立与对应broker之间的channel
                Bootstrap bootstrap = new Bootstrap();
                ChannelFuture channelFuture = bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        // 在多线程中，handler可能会被多次调用，因此每次都需要新建
                        // 这里不可使用netty只有标识作用的注解@Shareable，因为目前的handler实现中应有独立不共享变量
                        // 而且事实上@Shareable需要自己去考虑竞争问题
                        .handler(new ChannelInitializer<>() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline()
                                        // .addLast(new LoggingHandler(LogLevel.INFO))
                                        .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                                        .addLast(new MQConsumerHandler(invokeService, listenerService));
                            }
                        })
                        .connect(host, port)
                        .syncUninterruptibly();
                logger.info("Successfully created channel between consumer and broker {}:{}.", host, port);

                // 注册到对应broker
                ServiceEntry serviceEntry = new ServiceEntry(host, port, weight, groupName);
                BrokerRegisterRequest registerRequest = new BrokerRegisterRequest(snowFlake.nextId(), MethodType.CONSUMER_REGISTER, serviceEntry);
                MQCommonResponse response = IInvokeService.callServer(channelFuture.channel(), registerRequest, MQCommonResponse.class, invokeService, responseTimeoutMilliseconds);
                logger.info("Registration sent to broker {}:{}, got response {}.", host, port, response);

                // 客户端本地存储ChannelFuture
                RPCChannelFuture rpcChannelFuture = new RPCChannelFuture(host, port, weight, channelFuture);
                this.channelFutureList.add(rpcChannelFuture);
            }
            this.setEnableStatus(true);
            // 实例停机钩子并注册，ShutdownHook最终会调用传入的destroyable接口对象重写的destroyAll方法
            final IShutdownHook shutdownHook = new ClientShutdownHook(invokeService, this, waitTimeForRemainRequest);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    shutdownHook.shutdown();
                } catch (Exception e) {
                    throw new MQException(ConsumerResponseCode.CONSUMER_SHUTDOWN_ERROR);
                }
            }));
            logger.info("Message queue consumer server started.");
        } catch (Exception e) {
            logger.error("Error in starting message queue consumer");
            throw new MQException(ConsumerResponseCode.CONSUMER_INIT_FAILED);
        }
    }

    /**
     * 向所有已注册的broker订阅同一个消息，这要求producer只随机选一个broker发msg，不然consumer会收到重复消息
     */
    @Override
    public void subscribe(String topicName, String tagRegex) throws InterruptedException, JsonProcessingException {
        // 检查异步的consumer初始化任务是否已完成，包括创建与broker之间的channel以及注册至broker，体现在enable标志位
        while (!this.enable()) {
            Thread.sleep(10);
        }

        ConsumerSubscribeRequest request = new ConsumerSubscribeRequest(snowFlake.nextId(), MethodType.CONSUMER_SUB, groupName, topicName, tagRegex);

        for (RPCChannelFuture channelFuture :
                channelFutureList) {
            Channel channel = channelFuture.getChannelFuture().channel();

            MQCommonResponse response = Objects.requireNonNull(IInvokeService.callServer(channel, request, MQCommonResponse.class, invokeService, responseTimeoutMilliseconds));
            if (!MQCommonResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                throw new MQException(ConsumerResponseCode.CONSUMER_SUB_FAILED);
            }
        }

        localSubscribeInfoSet.add(new LocalSubscribeInfo(topicName, tagRegex));
    }

    /**
     * 向所有已注册的broker取消订阅消息
     */
    @Override
    public void unsubscribe(String topicName, String tagRegex) throws JsonProcessingException {
        ConsumerSubscribeRequest unSubReq = new ConsumerSubscribeRequest(snowFlake.nextId(), MethodType.CONSUMER_UNSUB, groupName, topicName, tagRegex);
        for (RPCChannelFuture channelFuture : channelFutureList) {
            Channel channel = channelFuture.getChannelFuture().channel();
            MQCommonResponse response = Objects.requireNonNull(IInvokeService.callServer(channel, unSubReq, MQCommonResponse.class, invokeService, responseTimeoutMilliseconds));
            if (!response.getResponseCode().equals(MQCommonResponseCode.SUCCESS.getCode())) {
                throw new MQException(ConsumerResponseCode.CONSUMER_UNSUB_FAILED);
            }
        }
        localSubscribeInfoSet.remove(new LocalSubscribeInfo(topicName, tagRegex));
    }

    @Override
    public void registerListener(IMQConsumerListener listener) {
        this.listenerService.register(listener);
    }

    @Override
    public void destroyAll() throws JsonProcessingException {
        // unsub->unregister
        localSubscribeInfoSet.forEach((e) -> {
            try {
                this.unsubscribe(e.getTopicName(), e.getTagRegex());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
        for (RPCChannelFuture channelFuture :
                channelFutureList) {
            Channel channel = channelFuture.getChannelFuture().channel();
            ServiceEntry serviceEntry = ChannelUtils.buildServiceEntry(channelFuture);
            BrokerRegisterRequest unregisterReq = new BrokerRegisterRequest(snowFlake.nextId(), MethodType.CONSUMER_UNREGISTER, serviceEntry);
            IInvokeService.callServer(channel, unregisterReq, null, invokeService, responseTimeoutMilliseconds);
        }
        workerGroup.shutdownGracefully();
    }
}
