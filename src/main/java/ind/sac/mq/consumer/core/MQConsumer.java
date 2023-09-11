package ind.sac.mq.consumer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.constant.BrokerConst;
import ind.sac.mq.broker.model.dto.RegisterRequest;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.dto.request.HeartbeatRequest;
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
import ind.sac.mq.common.util.SnowFlake;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import ind.sac.mq.consumer.constant.ConsumerConst;
import ind.sac.mq.consumer.constant.ConsumerResponseCode;
import ind.sac.mq.consumer.dto.request.ConsumeStatusUpdateRequest;
import ind.sac.mq.consumer.dto.request.ConsumerPullRequest;
import ind.sac.mq.consumer.dto.request.ConsumerSubscribeRequest;
import ind.sac.mq.consumer.dto.response.ConsumerPullResponse;
import ind.sac.mq.consumer.handler.ConsumerHandler;
import ind.sac.mq.consumer.record.SubscribeInfo;
import ind.sac.mq.consumer.service.listener.ConsumerListener;
import ind.sac.mq.consumer.service.listener.ConsumerListenerService;
import ind.sac.mq.consumer.service.listener.impl.ConsumerListenerContextImpl;
import ind.sac.mq.consumer.service.listener.impl.ConsumerListenerServiceImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MQConsumer extends Thread implements Destroyable {

    private static final Logger logger = LoggerFactory.getLogger(MQConsumer.class);

    private final String groupName;

    private final List<Channel> channels = new ArrayList<>();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();  // channel线程池

    private final SnowFlake snowFlake;

    private final InvokeService invokeService = new InvokeServiceImpl();

    private final ConsumerListenerService listenerService = new ConsumerListenerServiceImpl();

    // 如果在定时任务配置里遍历channel并调用，会出现各channel间消息混杂的问题
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);

    private final ScheduledThreadPoolExecutor consumeExecutor = new ScheduledThreadPoolExecutor(1);

    private int weight = ConsumerConst.DEFAULT_WEIGHT;  // 本消费者的权重
    private String brokerAddress;

    // 本地记录且同步此消费者的订阅信息，以便在消费者注销时批量取消订阅
    private final Set<SubscribeInfo> subscribeInfos = new HashSet<>();
    private long responseTimeout = ConsumerConst.DEFAULT_RESPONSE_TIMEOUT;  // 单位：毫秒

    private String delimiter = DelimiterUtil.DELIMITER;

    private boolean enable = false;

    private long waitTimeForRemainRequest = ConsumerConst.DEFAULT_WAIT_TIME_FOR_REMAIN_REQUEST;

    private long longPollTimeout = BrokerConst.DEFAULT_LONG_POLLING_PENDING_TIMEOUT + this.responseTimeout;

    private int pullSize = ConsumerConst.DEFAULT_PULL_SIZE;

    private int messagePullStopThreshold = ConsumerConst.DEFAULT_MESSAGE_PULL_STOP_THRESHOLD;

    private int messagePullRestartThreshold = ConsumerConst.DEFAULT_MESSAGE_PULL_RESTART_THRESHOLD;

    // 拉取得到的消息在本地的缓冲, 当生产速度大于消费速度时阻塞消息拉取，反之阻塞消费
    private final BlockingQueue<Message> messageBuffer = new ArrayBlockingQueue<>(messagePullStopThreshold);

    public MQConsumer(String groupName, String brokerAddress, int datacenterId, int machineId) {
        this.groupName = groupName;
        this.brokerAddress = brokerAddress;
        this.snowFlake = new SnowFlake(datacenterId, machineId);
    }

    public MQConsumer(String groupName) {
        this(groupName, ConsumerConst.DEFAULT_BROKER_ADDRESS, ConsumerConst.DEFAULT_DATACENTER_ID, ConsumerConst.DEFAULT_MACHINE_ID);
    }

    public MQConsumer() {
        this(ConsumerConst.DEFAULT_GROUP_NAME, ConsumerConst.DEFAULT_BROKER_ADDRESS, ConsumerConst.DEFAULT_DATACENTER_ID, ConsumerConst.DEFAULT_MACHINE_ID);
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
        this.longPollTimeout = BrokerConst.DEFAULT_LONG_POLLING_PENDING_TIMEOUT + responseTimeout;
    }

    public void setWaitTimeForRemainRequest(long waitTimeForRemainRequest) {
        this.waitTimeForRemainRequest = waitTimeForRemainRequest;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setPullSize(int pullSize) {
        this.pullSize = pullSize;
    }

    public void setMessagePullStopThreshold(int messagePullStopThreshold) {
        this.messagePullStopThreshold = messagePullStopThreshold;
    }

    public void setMessagePullRestartThreshold(int messagePullRestartThreshold) {
        this.messagePullRestartThreshold = messagePullRestartThreshold;
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
        logger.info("Message queue consumer start to run; groupName: {}, brokerAddress: {}", groupName, brokerAddress);
        try {
            final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf((this.delimiter));

            // 遍历建立消费者与其关联的那些brokers之间的channel，并且在对应broker储存本consumer的信息（注册）
            // 在RocketMQ中，消费者/生产者通过请求NameServer集群中的一个，来获得其所需的消息路由信息(brokers)
            // 这里目前没有实现路由匹配功能，所以消费者/生产者一方需要尽量多与broker建立通道，本项目选择消费者与所有broker建立通道
            List<RPCAddress> addressList = AddressUtil.splitAddrFromStr(brokerAddress);
            for (RPCAddress address :
                    addressList) {
                final String host = address.getHost();
                final int port = address.getPort();
                final int brokerWeight = address.getWeight();

                // 消费者作为客户端，建立与对应broker之间的channel
                Bootstrap bootstrap = new Bootstrap();
                ChannelFuture channelFuture = bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        // 在多线程中，handler可能会被多次调用，因此每次都需要新建
                        // 这里不可使用netty只有标识作用的注解@Shareable，因为目前的handler实现中有独立不共享变量
                        // 而且事实上@Shareable需要自己去考虑竞争问题
                        .handler(new ChannelInitializer<>() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline()
                                        // .addLast(new LoggingHandler(LogLevel.INFO))
                                        .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                                        .addLast(new ConsumerHandler(invokeService, listenerService));
                            }
                        })
                        .connect(host, port)
                        .syncUninterruptibly();
                logger.info("Successfully created channel between consumer and broker {}:{}.", host, port);

                // 注册到对应broker
                RegisterRequest registerRequest = new RegisterRequest(snowFlake.nextId(), MethodType.CONSUMER_REGISTER, this.groupName, this.weight);
                CommonResponse response = InvokeService.callServer(channelFuture.channel(), registerRequest, CommonResponse.class, invokeService, responseTimeout);
                logger.info("Registration sent to broker {}:{}, got response {}.", host, port, response);

                // 心跳配置
                scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
                    final HeartbeatRequest heartbeat = new HeartbeatRequest(snowFlake.nextId(), MethodType.CONSUMER_HEARTBEAT, this.groupName);
                    try {
                        InvokeService.callServer(channelFuture.channel(), heartbeat, CommonResponse.class, invokeService, responseTimeout);
                    } catch (Exception e) {
                        logger.error("[Heartbeat] Sent to server error!");
                        throw new RuntimeException(e);
                    }
                }, 5, 5, TimeUnit.SECONDS);

                // 长轮询配置
                scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
                    if (!subscribeInfos.isEmpty()) {
                        ConsumerPullRequest pullRequest = new ConsumerPullRequest(snowFlake.nextId(), MethodType.CONSUMER_LONG_POLLING, this.groupName, this.pullSize, this.subscribeInfos);
                        try {
                            ConsumerPullResponse pullResponse = Objects.requireNonNull(InvokeService.callServer(channelFuture.channel(), pullRequest, ConsumerPullResponse.class, invokeService, longPollTimeout));
                            for (Message message : pullResponse.getMessages()) {
                                messageBuffer.put(message);
                            }
                        } catch (Exception e) {
                            logger.error("Error occur in long polling: ", e);
                            throw new RuntimeException(e);
                        }
                    }
                }, 1000, 500, TimeUnit.MILLISECONDS);

                // 解耦消息的拉取与消费
                consumeExecutor.scheduleWithFixedDelay(() -> {
                    try {
                        Message message = messageBuffer.take();
                        ConsumeStatus status = this.listenerService.consume(message, new ConsumerListenerContextImpl());
                        ConsumeStatusUpdateRequest consumeStatusUpdateRequest = new ConsumeStatusUpdateRequest(snowFlake.nextId(), MethodType.CONSUMER_PULL_ACK, message.getTraceId(), status);
                        InvokeService.callServer(channelFuture.channel(), consumeStatusUpdateRequest, null, invokeService, responseTimeout);
                    } catch (Exception e) {
                        logger.error("Error occur in consuming : ", e);
                        throw new RuntimeException(e);
                    }
                }, 1000, 100, TimeUnit.MILLISECONDS);

                // 客户端本地存储channel
                this.channels.add(channelFuture.channel());
            }

            // 新建停机钩子并注册，ShutdownHook最终会调用传入的destroyable接口对象重写的destroyAll方法
            final ShutdownHook shutdownHook = new ClientShutdownHook(invokeService, this, waitTimeForRemainRequest);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    shutdownHook.shutdown();
                } catch (Exception e) {
                    throw new MQException(ConsumerResponseCode.CONSUMER_SHUTDOWN_ERROR);
                }
            }));

            this.setEnableStatus(true);
            logger.info("Message queue consumer server started.");
        } catch (Exception e) {
            logger.error("Error in starting message queue consumer");
            throw new MQException(ConsumerResponseCode.CONSUMER_INIT_FAILED);
        }
    }

    /**
     * 向所有已注册的broker订阅同一个消息
     */
    public void subscribe(String topicName, String tagRegex) throws InterruptedException, JsonProcessingException {
        // 检查异步的consumer初始化任务是否已完成，包括创建与broker之间的channel以及注册至broker，体现在enable标志位
        while (!this.enable()) {
            Thread.sleep(10);
        }

        final ConsumerSubscribeRequest subscribeRequest = new ConsumerSubscribeRequest(snowFlake.nextId(), MethodType.CONSUMER_SUB, groupName, topicName, tagRegex);

        for (Channel channel : channels) {
            CommonResponse response = Objects.requireNonNull(InvokeService.callServer(channel, subscribeRequest, CommonResponse.class, invokeService, responseTimeout));
            if (!CommonResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
                throw new MQException(ConsumerResponseCode.CONSUMER_SUB_FAILED);
            }
        }

        subscribeInfos.add(new SubscribeInfo(topicName, tagRegex));
    }

    /**
     * 向所有已注册的broker取消订阅消息
     */
    public void unsubscribe(String topicName, String tagRegex) throws JsonProcessingException {
        ConsumerSubscribeRequest unSubReq = new ConsumerSubscribeRequest(snowFlake.nextId(), MethodType.CONSUMER_UNSUB, groupName, topicName, tagRegex);
        for (Channel channel : channels) {
            CommonResponse response = Objects.requireNonNull(InvokeService.callServer(channel, unSubReq, CommonResponse.class, invokeService, responseTimeout));
            if (!response.getResponseCode().equals(CommonResponseCode.SUCCESS.getCode())) {
                throw new MQException(ConsumerResponseCode.CONSUMER_UNSUB_FAILED);
            }
        }
        subscribeInfos.remove(new SubscribeInfo(topicName, tagRegex));
    }

    public void registerListener(ConsumerListener listener) {
        this.listenerService.register(listener);
    }

    @Override
    public void destroy() throws JsonProcessingException {
        // unsub->unregister
        subscribeInfos.forEach((e) -> {
            try {
                this.unsubscribe(e.topicName(), e.tagRegex());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
        for (Channel channel :
                channels) {
            RegisterRequest unregisterReq = new RegisterRequest(snowFlake.nextId(), MethodType.CONSUMER_UNREGISTER, this.groupName, this.weight);
            InvokeService.callServer(channel, unregisterReq, null, invokeService, responseTimeout);
        }
        scheduledThreadPoolExecutor.shutdown();
        consumeExecutor.shutdown();
        workerGroup.shutdownGracefully();
    }
}
