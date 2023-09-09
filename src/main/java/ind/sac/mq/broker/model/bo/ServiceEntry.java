package ind.sac.mq.broker.model.bo;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * Info of one client, including its group name, rpc remote host&port(got from channel), client weight and last access time. <br/>
 * All of them are constructed&used by server(broker), some info sent from client. <br/>
 * Using case: registration, heartbeat, recording service info based on channel.
 */
public class ServiceEntry {

    private String groupName;

    private Channel channel;

    private int weight;

    private long lastAccessTime;

    public ServiceEntry() {
    }

    public ServiceEntry(String groupName, Channel channel) {
        this.groupName = groupName;
        this.channel = channel;
    }

    public ServiceEntry(String groupName, Channel channel, int weight) {
        this.groupName = groupName;
        this.channel = channel;
        this.weight = weight;
    }

    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    public String getLocalHost() {
        return this.getLocalAddress().getAddress().getHostAddress();
    }

    public int getLocalPort() {
        return this.getLocalAddress().getPort();
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    public String getRemoteHost() {
        return this.getRemoteAddress().getAddress().getHostAddress();
    }

    public int getRemotePort() {
        return this.getRemoteAddress().getPort();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public String toString() {
        return "ServiceEntry{" +
                "groupName='" + groupName + '\'' +
                ", channel=" + channel +
                ", weight=" + weight +
                ", lastAccessTime=" + lastAccessTime +
                '}';
    }
}
