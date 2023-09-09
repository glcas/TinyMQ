package ind.sac.mq.broker.core;

import ind.sac.mq.broker.constant.BrokerConst;

class MQBrokerTest {

    private static final int brokerNum = 1;

    public static void main(String[] args) {
        for (int i = 0; i < brokerNum; i++) {
            MQBroker broker = new MQBroker(BrokerConst.DEFAULT_PORT + i);
            broker.start();
        }
    }
}