package ind.sac.mq.producer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.MQCommonRequest;
import ind.sac.mq.common.dto.response.MQCommonResponse;
import ind.sac.mq.common.exception.MQCommonResponseCode;
import ind.sac.mq.common.exception.MQException;
import ind.sac.mq.common.rpc.RPCMessageDTO;
import ind.sac.mq.common.support.invoke.IInvokeService;
import ind.sac.mq.common.support.invoke.impl.InvokeService;
import ind.sac.mq.common.utils.DelimiterUtil;
import ind.sac.mq.common.utils.JsonUtil;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQProducer extends Thread implements IMQProducer {

    private static final Logger logger = LoggerFactory.getLogger(MQProducer.class);

    private final String groupName;

    private final int port;

    private final SnowFlake snowFlake;

    private final IInvokeService invokeService = new InvokeService();

    private String brokerAddr = "";
    private long responseTimeoutMilliseconds = 5000;
    private boolean enable = false;
    private ChannelFuture channelFuture;
    private ChannelHandler channelHandler;
    private String delimiter = DelimiterUtil.DELIMITER;

    public MQProducer(String groupName, int port, int datacenterId, int machineId) {
        this.groupName = groupName;
        this.port = port;
        this.snowFlake = new SnowFlake(datacenterId, machineId);
    }

    public MQProducer(String groupName) {
        this(groupName, ProducerConst.DEFAULT_PORT, ProducerConst.DEFAULT_DATACENTER_ID, ProducerConst.DEFAULT_MACHINE_ID);
    }

    public MQProducer() {
        this(ProducerConst.DEFAULT_GROUP_NAME, ProducerConst.DEFAULT_PORT, ProducerConst.DEFAULT_DATACENTER_ID, ProducerConst.DEFAULT_MACHINE_ID);
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

    private void initChannelHandler() {
        final MQProducerHandler mqProducerHandler = new MQProducerHandler();
        mqProducerHandler.setInvokeService(invokeService);

        // 在多线程中，handler可能会被多次调用，因此每次都需要新建
        // 这里不可使用netty只有标识作用的注解@Sharable，因为目前的handler实现中应有独立不共享变量
        // 而且事实上@Sharable需要自己去考虑竞争问题
        this.channelHandler = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline()
                        .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH, DelimiterUtil.getByteBuf(delimiter)))
                        .addLast(mqProducerHandler);
            }
        };
    }

    @Override
    public synchronized void run() {
        logger.info("Message queue start to run client; groupName: {}, port: {}, brokerAddress: {}", groupName, port, brokerAddr);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        this.initChannelHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            channelFuture = bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(channelHandler);
                        }
                    })
                    .connect("localhost", port)
                    .syncUninterruptibly();
            enable = true;
            logger.info("Message queue producer started client, port: " + port);
        } catch (Exception e) {
            logger.error("Error while starting message queue producer", e);
            throw new MQException(ProducerResponseCode.PRODUCER_INIT_FAILED);
        } finally {
            channelFuture.channel().closeFuture().syncUninterruptibly();
            logger.info("Message queue producer bootstrap client closed.");
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public SendResult syncSend(MQCommonRequest mqMessage) throws JsonProcessingException {
        long messageId = this.snowFlake.nextId();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.PRODUCER_SEND_MESSAGE);
        MQCommonResponse response = callServer(mqMessage, MQCommonResponse.class);
        if (MQCommonResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return SendResult.of(messageId, SendStatus.SUCCESS);
        } else {
            return SendResult.of(messageId, SendStatus.FAIL);
        }
    }

    @Override
    public SendResult onewaySend(MQCommonRequest mqMessage) throws JsonProcessingException {
        long messageId = this.snowFlake.nextId();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.PRODUCER_SEND_MESSAGE);
        this.callServer(mqMessage, null);
        return SendResult.of(messageId, SendStatus.FAIL);
    }

    /**
     * 泛型T,R的上界由extends确定（至C/S互调版本二者均无子类）
     *
     * @param commonRequest 请求
     * @param responseClass 响应类
     * @param <T>           MQCommonRequest
     * @param <R>           MQCommonResponse
     * @return 响应
     */
    public <T extends MQCommonRequest, R extends MQCommonResponse> R callServer(T commonRequest, Class<R> responseClass) throws JsonProcessingException {
        final long traceId = commonRequest.getTraceId();

        RPCMessageDTO rpcMessageDTO = new RPCMessageDTO();
        rpcMessageDTO.setRequestId(traceId);
        rpcMessageDTO.setTime(System.currentTimeMillis());
        rpcMessageDTO.setMethodType(commonRequest.getMethodType());
        rpcMessageDTO.setRequest(true);
        rpcMessageDTO.setData(JsonUtil.writeAsJsonString(commonRequest));  // commonRequest中只有traceId和methodType两字段，这一行目前只是用来占位

        invokeService.addRequest(traceId, responseTimeoutMilliseconds);

        // 序列化
        ByteBuf byteBuf = DelimiterUtil.getDelimitedMessageBuffer(rpcMessageDTO);
        // TODO: can consider load balance
        channelFuture.channel().writeAndFlush(byteBuf);

        if (responseClass == null) {
            // callServer方法调用时传入的响应类型为无，意味着这次调用发送的消息是单向的，不需要响应
            return null;
        } else {
            // 响应获取，getResponse方法在响应map中查id，查不到时在此等待
            RPCMessageDTO messageDTO = invokeService.getResponse(traceId);
            if (MQCommonResponseCode.TIMEOUT.getCode().equals(messageDTO.getResponseCode())) {
                throw new MQException(MQCommonResponseCode.TIMEOUT);
            }
            String responseData = messageDTO.getData();
            return JsonUtil.parseJson(responseData, responseClass);
        }
    }
}
