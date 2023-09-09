package ind.sac.mq.consumer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.constant.BrokerConst;
import ind.sac.mq.consumer.constant.ConsumeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

class MQConsumerTest {

    private static final int consumerNum = 4;

    private static final int brokerNum = 2;

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        StringBuilder brokerAddrBuilder = new StringBuilder();
        for (int i = 0; i < brokerNum; i++) {
            brokerAddrBuilder.append("localhost:").append(BrokerConst.DEFAULT_PORT + i).append(",");
        }

        for (int i = 0; i < consumerNum; i++) {
            String consumerID = String.valueOf(i + 1);
            MQConsumer consumer = new MQConsumer(consumerID);
            consumer.setBrokerAddress(brokerAddrBuilder.toString());
            // consumer.setResponseTimeout(Integer.MAX_VALUE);

            // 重写的是run方法，但直接调用会在本线程中执行；而调用start方法会新开线程执行run
            consumer.start();

            // consumer.subscribe("TOPIC", "(?i)TAG_[a-zA-Z]");  // 匹配标签TAG_*（不区分大小写），其中*为任意字母
            consumer.subscribe("TOPIC", "TAG_" + (i + 1));
            consumer.registerListener((message, context) -> {
                Logger logger = LoggerFactory.getLogger(MQConsumerTest.class);
                logger.info("Consumer " + consumerID + " received message: " + "Topics: {}, tags: {}, decoded payload: {}", message.getTopic(), message.getTags(), new String(message.getPayload(), StandardCharsets.UTF_8));
                return ConsumeStatus.SUCCESS;
            });
        }
    }
}