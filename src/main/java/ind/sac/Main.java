package ind.sac;

import ind.sac.mq.consumer.core.MqConsumerPush;

public class Main {
    public static void main(String[] args) {
        MqConsumerPush mqConsumerPush = new MqConsumerPush();
        mqConsumerPush.start();
    }
}