package ind.sac.mq.common.dto.request;

import ind.sac.mq.common.constant.MethodType;

public class MQHeartbeatRequest extends MQCommonRequest {

    private String host;

    private int port;

    private long time;

    public MQHeartbeatRequest() {
    }

    public MQHeartbeatRequest(long traceId, MethodType methodType, String host, int port, long time) {
        super(traceId, methodType);
        this.host = host;
        this.port = port;
        this.time = time;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
