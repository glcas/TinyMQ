package ind.sac.mq.consumer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.constant.BrokerConst;
import ind.sac.mq.consumer.constant.ConsumeStatus;

import java.nio.charset.StandardCharsets;

class MQConsumerTest {

    private static final int consumerNum = 3;

    private static final int brokerNum = 5;

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        StringBuilder brokerAddrBuilder = new StringBuilder();
        for (int i = 0; i < brokerNum; i++) {
            brokerAddrBuilder.append("localhost:").append(BrokerConst.DEFAULT_PORT + i).append(",");
        }

        for (int i = 0; i < consumerNum; i++) {
            String consumerID = String.valueOf(i + 1);
            MQConsumer consumer = new MQConsumer(consumerID);
            consumer.setBrokerAddress(brokerAddrBuilder.toString());

            // 重写的是run方法，但直接调用会在本线程中执行；而调用start方法会新开线程执行run
            consumer.start();

            // consumer.subscribe("TOPIC", "(?i)TAG_[a-zA-Z]");  // 匹配标签TAG_*（不区分大小写），其中*为任意字母
            consumer.subscribe("TOPIC", "TAG_" + (i + 1));
            consumer.registerListener((message, context) -> {
                System.out.print("Consumer " + consumerID + " successfully received message with via broker; ");
                System.out.printf("Topics: %s, tags: %s, decoded payload: %s\n", message.getTopic(), message.getTags(), new String(message.getPayload(), StandardCharsets.UTF_8));
                return ConsumeStatus.SUCCESS;
            });
        }
    }
}