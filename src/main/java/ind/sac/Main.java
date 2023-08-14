package ind.sac;

import ind.sac.mq.consumer.core.MQConsumer;

public class Main {
    public static void main(String[] args) {
        MQConsumer mqConsumer = new MQConsumer();
        mqConsumer.start();
    }
}