package ind.sac;

import ind.sac.mq.consumer.core.MQPushedConsumer;

public class Main {
    public static void main(String[] args) {
        MQPushedConsumer mqPushedConsumer = new MQPushedConsumer();
        mqPushedConsumer.start();
    }
}