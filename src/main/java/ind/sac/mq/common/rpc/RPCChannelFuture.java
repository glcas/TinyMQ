package ind.sac.mq.common.rpc;

import io.netty.channel.ChannelFuture;

/**
 * Include info of channel future(including channel features) and URL
 */
public class RPCChannelFuture extends RPCAddress {

    private ChannelFuture channelFuture;

    public RPCChannelFuture(String host, int port, int weight, ChannelFuture channelFuture) {
        super(host, port, weight);
        this.channelFuture = channelFuture;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }
}
