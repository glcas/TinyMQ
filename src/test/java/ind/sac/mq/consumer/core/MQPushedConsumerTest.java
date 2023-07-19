package ind.sac.mq.consumer.core;

class MQPushedConsumerTest {
    public static void main(String[] args) {
        MQPushedConsumer mqPushedConsumer = new MQPushedConsumer();
        // 重写的是run方法，但直接调用会在本线程中执行；而调用start方法会新开线程执行run
        mqPushedConsumer.start();
    }
}