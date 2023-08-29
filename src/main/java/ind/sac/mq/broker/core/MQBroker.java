package ind.sac.mq.broker.core;

import ind.sac.mq.broker.constant.BrokerConst;
import ind.sac.mq.broker.constant.BrokerResponseCode;
import ind.sac.mq.broker.handler.BrokerHandler;
import ind.sac.mq.broker.service.BrokerConsumerService;
import ind.sac.mq.broker.service.BrokerPersistenceService;
import ind.sac.mq.broker.service.BrokerProducerService;
import ind.sac.mq.broker.service.BrokerPushService;
import ind.sac.mq.broker.service.impl.BrokerConsumerServiceImpl;
import ind.sac.mq.broker.service.impl.BrokerPersistenceServiceImpl;
import ind.sac.mq.broker.service.impl.BrokerProducerServiceImpl;
import ind.sac.mq.broker.service.impl.BrokerPushServiceImpl;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.service.invoke.InvokeService;
import ind.sac.mq.common.service.invoke.impl.InvokeServiceImpl;
import ind.sac.mq.common.util.DelimiterUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQBroker extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MQBroker.class);

    private final int port;

    private final InvokeService invokeService = new InvokeServiceImpl();

    private BrokerConsumerService brokerConsumerService = new BrokerConsumerServiceImpl();

    private BrokerProducerService brokerProducerService = new BrokerProducerServiceImpl();

    private BrokerPersistenceService brokerPersistenceService = new BrokerPersistenceServiceImpl();

    private BrokerPushService brokerPushService = new BrokerPushServiceImpl();

    // 单位：毫秒
    private long responseTimeout = 5000;

    public MQBroker(int port) {
        this.port = port;
    }

    public MQBroker() {
        this(BrokerConst.DEFAULT_PORT);
    }

    public void setBrokerConsumerService(@NotNull BrokerConsumerService brokerConsumerService) {
        this.brokerConsumerService = brokerConsumerService;
    }

    public void setBrokerProducerService(@NotNull BrokerProducerService brokerProducerService) {
        this.brokerProducerService = brokerProducerService;
    }

    public void setBrokerPersistenceService(@NotNull BrokerPersistenceService brokerPersistenceService) {
        this.brokerPersistenceService = brokerPersistenceService;
    }

    public void setBrokePushService(@NotNull BrokerPushService brokerPushService) {
        this.brokerPushService = brokerPushService;
    }

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    private ChannelHandler initChannelHandler() {
        return new BrokerHandler(invokeService, brokerConsumerService, brokerProducerService, brokerPersistenceService, brokerPushService, responseTimeout);
    }

    @Override
    public void run() {
        logger.info("Message queue broker start to listening on port {}", port);
        /*
          EventLoop是netty封装抽象的thread
          消息流：client->bossGroup(parentGroup)->workerGroup(childGroup)->handler
          服务器的bossGroup不断监听是否有客户端连接，若发现则为其初始化资源，然后从workerGroup中选出一个绑定至对应客户端连接
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf(DelimiterUtil.DELIMITER);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    // 这里添加的两个handler都是inbound，顺序执行
                                    // 如果是outbound的就是逆序，ctx.write(&flush)会将写的内容首先送至channel出站方向
                                    .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                                    .addLast(initChannelHandler());
                        }
                    })
                    // 客户端连接请求等待队列大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly();

            channelFuture.channel().closeFuture().syncUninterruptibly();
            logger.info("Message queue broker closed.");
        } catch (Exception e) {
            logger.error("Message queue broker start failed.", e);
            throw new MQException(BrokerResponseCode.BROKER_INIT_FAILED);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
