package ind.sac.mq.consumer.api;

public interface IMQConsumer {

    void subscribe(String topicName, String tagRegax);

    void registerListener(final IMQConsumerListener listener);

}
