package ind.sac.mq.consumer.core;

import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.support.invoke.impl.InvokeService;
import ind.sac.mq.common.utils.DelimiterUtil;
import ind.sac.mq.consumer.api.IMQConsumer;
import ind.sac.mq.consumer.api.IMQConsumerListener;
import ind.sac.mq.consumer.constant.ConsumerConst;
import ind.sac.mq.consumer.constant.ConsumerResponseCode;
import ind.sac.mq.consumer.handler.MQConsumerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 推送式消费者，被动接受被推送的消息，相当于服务端
 */
public class MQPushedConsumer extends Thread implements IMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MQPushedConsumer.class);

    private final String groupName;

    private final int port;

    private String brokerAddr = "";
    private final IInvokeService invokeService = new InvokeService();
    private String delimiter = DelimiterUtil.DELIMITER;

    public MQPushedConsumer(String groupName, int port) {
        this.groupName = groupName;
        this.port = port;
    }

    public MQPushedConsumer(String groupName) {
        this(groupName, ConsumerConst.DEFAULT_PORT);
    }

    public MQPushedConsumer() {
        this(ConsumerConst.DEFAULT_GROUP_NAME, ConsumerConst.DEFAULT_PORT);
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void run() {
        logger.info("Message queue consumer start to run; groupName: {}, port: {},brokerAddress: {}", groupName, port, brokerAddr);
        // EventLoop是netty封装抽象的thread
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf((delimiter));
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(workerGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            // 这里添加的两个handler都是inbound，顺序执行
                            // 如果是outbound的就是逆序，ctx.write(&flush)会送至channel出站方向
                            channel.pipeline()
                                    .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, delimiterBuf))
                                    .addLast(new MQConsumerHandler(invokeService));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly();
            logger.info("Message queue consumer server started, listening port: " + port);
            channelFuture.channel().closeFuture().syncUninterruptibly();
            logger.info("Message queue consumer bootstrap client closed.");
        } catch (Exception e) {
            logger.error("error in starting message queue consumer");
            throw new MQException(ConsumerResponseCode.CONSUMER_INIT_FAILED);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void subscribe(String topicName, String tagRegex) {

    }

    @Override
    public void registerListener(IMQConsumerListener listener) {

    }
}
