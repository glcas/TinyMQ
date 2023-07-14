package ind.sac.mq.consumer.api;

public interface IMqConsumer {

    void subscribe(String topicName, String tagRegax);

    void registerListener(final IMqConsumerListener listener);

}
