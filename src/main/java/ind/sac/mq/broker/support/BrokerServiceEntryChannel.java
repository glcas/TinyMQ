package ind.sac.mq.broker.support;

import io.netty.channel.Channel;

/**
 * Include infos of channel, last access time, group name, rpc address&port. <p/>
 * Only used inner broker class.
 */
public class BrokerServiceEntryChannel extends ServiceEntry {

    private Channel channel;

    private long lastAccessTime;

    public BrokerServiceEntryChannel() {
    }

    public BrokerServiceEntryChannel(String address, int port) {
        super(address, port);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
