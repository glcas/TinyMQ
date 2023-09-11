package ind.sac.mq.producer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.broker.constant.BrokerConst;
import ind.sac.mq.common.dto.Message;
import ind.sac.mq.common.util.JsonUtil;
import ind.sac.mq.producer.dto.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class MQProducerTest {

    private static final int producerNum = 3;

    private static final int brokerNum = 2;

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        StringBuilder brokerAddrBuilder = new StringBuilder();
        for (int i = 0; i < brokerNum; i++) {
            brokerAddrBuilder.append("localhost:").append(BrokerConst.DEFAULT_PORT + i).append(",");
        }

        for (int i = 0; i < producerNum; i++) {
            MQProducer producer = new MQProducer();
            producer.setBrokerAddress(brokerAddrBuilder.toString());
            // producer.setResponseTimeout(Integer.MAX_VALUE);
            producer.start();

            Message message = new Message();
            message.setTopic("TOPIC");
            message.setTags(Arrays.asList("TAG_" + (i + 1), "TAG_" + (i + 2)));
            message.setPayload(("Consumer " + (i + 1) + "&" + (i + 2) + " should received this message.").getBytes(StandardCharsets.UTF_8));
            SendResult sendResult = producer.send(message);

            Logger logger = LoggerFactory.getLogger(MQProducerTest.class);
            logger.info("Producer" + (i + 1) + " send result: " + JsonUtil.writeAsJsonString(sendResult));
        }
    }
}