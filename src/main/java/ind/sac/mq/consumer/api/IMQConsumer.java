package ind.sac.mq.consumer.api;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IMQConsumer {

    void subscribe(String topicName, String tagRegex) throws InterruptedException, JsonProcessingException;

    void unsubscribe(String topicName, String tagRegex) throws JsonProcessingException;

    void registerListener(final IMQConsumerListener listener);

}
