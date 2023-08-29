package ind.sac.mq.common.util;

public class SnowFlake {

    // 起始时间戳，2023-04-23 23:04:23
    private final static long START_TIMESTAMP = 1682526263000L;

    private final static int DATACENTER_BIT = 5;
    private final static int MACHINE_BIT = 5;
    private final static int SEQUENCE_BIT = 12;

    /**
     * long有64位；-1为原码1的补码（1按位取反再+1），即-1L=0xFFFFFFFF
     * 然后左移num位再取反，得到高位为0，低num位为1的最大值
     * -1是int类型的整数，在执行位运算时，-1会被转换成long型
     */
    private final static long MAX_DATACENTER_NUM = ~(-1 << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1 << MACHINE_BIT);
    private final static long MAX_SEQUENCE_NUM = ~(-1 << SEQUENCE_BIT);

    private final static int MACHINE_SEG_LEFT = SEQUENCE_BIT;
    private final static int DATACENTER_SEG_LEFT = MACHINE_BIT + MACHINE_SEG_LEFT;
    private final static int TIMESTAMP_SEG_LEFT = DATACENTER_BIT + DATACENTER_SEG_LEFT;
    private final long datacenterId;
    private final long machineId;
    private long lastTimestamp = -1;
    private long sequence = 0;

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID should be within [MAX_DATACENTER_NUM,0]");
        } else if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("Datacenter ID should be within [MAX_DATACENTER_NUM,0]");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    // 同一时间只能有一个线程执行该方法，多线程执行到此时需要被synchronized
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Typical clock-moved-back problem of snowflake algorithm occurred.");
        } else if (currentTimestamp == lastTimestamp) {
            // 相同毫秒内，若序列号未达最大值则增长之，否则忙等待至下一毫秒
            if (sequence < MAX_SEQUENCE_NUM) {
                sequence++;
            } else {
                do {
                    currentTimestamp = System.currentTimeMillis();
                } while ((currentTimestamp == lastTimestamp));
                sequence = 0;
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = currentTimestamp;
        return (currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SEG_LEFT
                | datacenterId << DATACENTER_SEG_LEFT
                | machineId << MACHINE_SEG_LEFT
                | sequence;
    }
}
