package ind.sac.mq.producer.core;

import ind.sac.mq.common.dto.MqMessage;
import ind.sac.mq.common.exception.MqException;
import ind.sac.mq.producer.api.IMqProducer;
import ind.sac.mq.producer.constant.ProducerConst;
import ind.sac.mq.producer.constant.ProducerResponseCode;
import ind.sac.mq.producer.dto.SendResult;
import ind.sac.mq.producer.handler.MqProducerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqProducer extends Thread implements IMqProducer {

    private static final Logger logger = LoggerFactory.getLogger(MqProducer.class);

    private final String groupName;

    private final int port;

    private String brokerAddr = "";

    public MqProducer(String groupName, int port) {
        this.groupName = groupName;
        this.port = port;
    }

    public MqProducer(String groupName) {
        this(groupName, ProducerConst.DEFAULT_PORT);
    }

    public MqProducer() {
        this(ProducerConst.DEFAULT_GROUP_NAME, ProducerConst.DEFAULT_PORT);
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    @Override
    public void run() {
        logger.info("Message queue start to run client; groupName: {}, port: {}, brokerAddress: {}", groupName, port, brokerAddr);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture channelFuture = bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(new MqProducerHandler());
                        }
                    })
                    .connect("localhost", port)
                    .syncUninterruptibly();
            logger.info("Message queue producer started client, listening on: " + port);
            channelFuture.channel().closeFuture().syncUninterruptibly();
            logger.info("Message queue producer bootstrap client closed.");
        } catch (Exception e) {
            logger.error("Error while starting message queue producer", e);
            throw new MqException(ProducerResponseCode.PRODUCER_INIT_FAILED);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public SendResult syncSend(MqMessage mqMessage) {
        return null;
    }

    @Override
    public SendResult onewaySend(MqMessage mqMessage) {
        return null;
    }
}
