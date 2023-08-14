package ind.sac.mq.producer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.dto.request.MQRequestMessage;
import ind.sac.mq.common.utils.JsonUtil;
import ind.sac.mq.producer.dto.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class MQProducerTest {

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        MQProducer mqProducer = new MQProducer();
        mqProducer.start();
        while (!mqProducer.isEnable()) {
            Thread.sleep(10);
        }

        MQRequestMessage mqMessage = new MQRequestMessage();
        mqMessage.setTopic("TOPIC");
        mqMessage.setTags(Arrays.asList("TAG_A", "TAG_B"));
        mqMessage.setPayload("Hello, world!".getBytes(StandardCharsets.UTF_8));
        SendResult sendResult = mqProducer.syncSend(mqMessage);

        System.out.println("Send result: " + JsonUtil.writeAsJsonString(sendResult));
    }

}