package ind.sac.mq.broker.support;

import ind.sac.mq.common.rpc.RPCAddress;

/**
 * Include info of group name, rpc host&port.
 */
public class ServiceEntry extends RPCAddress {

    private String groupName;

    public ServiceEntry() {
    }

    public ServiceEntry(String address, int port) {
        super(address, port);
    }

    public ServiceEntry(String host, int port, int weight, String groupName) {
        super(host, port, weight);
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "ServiceEntry{" +
                "groupName='" + groupName + '\'' +
                '}';
    }
}
