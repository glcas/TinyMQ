package ind.sac.mq.consumer.core;

import ind.sac.mq.common.exception.MqException;
import ind.sac.mq.consumer.api.IMqConsumer;
import ind.sac.mq.consumer.api.IMqConsumerListener;
import ind.sac.mq.consumer.constant.ConsumerConst;
import ind.sac.mq.consumer.constant.ConsumerResponseCode;
import ind.sac.mq.consumer.handler.MqConsumerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqConsumerPush extends Thread implements IMqConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MqConsumerPush.class);

    private final String groupName;

    private final int port;

    private String brokerAddr = "";

    public MqConsumerPush(String groupName, int port) {
        this.groupName = groupName;
        this.port = port;
    }

    public MqConsumerPush(String groupName) {
        this(groupName, ConsumerConst.DEFAULT_PORT);
    }

    public MqConsumerPush() {
        this(ConsumerConst.DEFAULT_GROUP_NAME, ConsumerConst.DEFAULT_PORT);
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    @Override
    public void run() {
        logger.info("Message queue consumer start to run; groupName: {}, port: {},brokerAddress: {}", groupName, port, brokerAddr);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(workerGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new MqConsumerHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly();
            logger.info("Message queue consumer client started, listening port: " + port);
            channelFuture.channel().closeFuture().syncUninterruptibly();
            logger.info("Message queue consumer bootstrap client closed.");
        } catch (Exception e) {
            logger.error("error in starting message queue consumer");
            throw new MqException(ConsumerResponseCode.CONSUMER_INIT_FAILED);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void subscribe(String topicName, String tagRegex) {

    }

    @Override
    public void registerListener(IMqConsumerListener listener) {

    }

}
