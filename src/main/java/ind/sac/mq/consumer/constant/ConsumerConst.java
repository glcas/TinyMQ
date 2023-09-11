package ind.sac.mq.consumer.constant;

public class ConsumerConst {

    public static final String DEFAULT_GROUP_NAME = "CONSUMER_DEFAULT_GROUP_NAME";

    public static final String DEFAULT_BROKER_ADDRESS = "localhost:2304";

    public static final int DEFAULT_DATACENTER_ID = 1;

    public static final int DEFAULT_MACHINE_ID = 1;

    public static final int DEFAULT_WEIGHT = 0;

    public static final int DEFAULT_RESPONSE_TIMEOUT = 5000;

    public static final int DEFAULT_WAIT_TIME_FOR_REMAIN_REQUEST = 60 * 1000;

    public static final int DEFAULT_PULL_SIZE = 10;

    public static final int DEFAULT_MESSAGE_PULL_STOP_THRESHOLD = 500;

    public static final int DEFAULT_MESSAGE_PULL_RESTART_THRESHOLD = 400;
}
