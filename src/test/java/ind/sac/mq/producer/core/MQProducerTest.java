package ind.sac.mq.producer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.dto.request.MQCommonRequest;
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

        MQCommonRequest mqMessage = new MQCommonRequest();
        mqMessage.setTopic("TOPIC");
        mqMessage.setTags(Arrays.asList("tag1", "tag2"));
        mqMessage.setPayload("Hello, world!".getBytes(StandardCharsets.UTF_8));
        SendResult sendResult = mqProducer.syncSend(mqMessage);

        System.out.println(JsonUtil.writeAsJsonString(sendResult));
    }

}