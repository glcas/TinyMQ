package ind.sac.mq.common.rpc;

public class RPCAddress {

    private String host;

    private int port;

    private int weight;

    public RPCAddress() {
    }

    public RPCAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RPCAddress(String host, int port, int weight) {
        this.host = host;
        this.port = port;
        this.weight = weight;
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
