package ind.sac.mq.consumer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.response.ConsumeStatus;

import java.nio.charset.StandardCharsets;

class MQConsumerTest {
    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        MQConsumer mqConsumer = new MQConsumer();
        // 重写的是run方法，但直接调用会在本线程中执行；而调用start方法会新开线程执行run
        mqConsumer.start();
        mqConsumer.subscribe("TOPIC", "(?i)TAG_[a-zA-Z]");  // 匹配标签TAG_*（不区分大小写），其中*为任意字母
        mqConsumer.registerListener((message, context) -> {
            System.out.println("Successfully received message with via broker!");
            System.out.printf("Topics: %s, tags: %s, decoded payload: %s\n", message.getTopic(), message.getTags(), new String(message.getPayload(), StandardCharsets.UTF_8));
            return ConsumeStatus.SUCCESS;
        });
    }
}