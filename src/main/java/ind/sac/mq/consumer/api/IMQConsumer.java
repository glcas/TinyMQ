package ind.sac.mq.consumer.api;

public interface IMQConsumer {

    void subscribe(String topicName, String tagRegex);

    void registerListener(final IMQConsumerListener listener);

}
