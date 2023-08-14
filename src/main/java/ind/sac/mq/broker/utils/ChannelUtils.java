package ind.sac.mq.broker.utils;

import ind.sac.mq.broker.support.BrokerServiceEntryChannel;
import ind.sac.mq.broker.support.ServiceEntry;
import ind.sac.mq.common.rpc.RPCChannelFuture;
import io.netty.channel.Channel;

public class ChannelUtils {

    /**
     * Construct service entry instance, info comes from existed RPC channel future.
     *
     * @param rpcChannelFuture RPC channel future
     * @return new service entry
     */
    public static ServiceEntry buildServiceEntry(RPCChannelFuture rpcChannelFuture) {
        ServiceEntry serviceEntry = new ServiceEntry();

        // extends from RPCAddress
        serviceEntry.setHost(rpcChannelFuture.getHost());
        serviceEntry.setPort(rpcChannelFuture.getPort());
        serviceEntry.setWeight(rpcChannelFuture.getWeight());

        return serviceEntry;
    }

    public static BrokerServiceEntryChannel buildEntryChannel(ServiceEntry serviceEntry, Channel channel) {
        BrokerServiceEntryChannel result = new BrokerServiceEntryChannel();

        result.setChannel(channel);

        // extends from ServiceEntry
        result.setGroupName(serviceEntry.getGroupName());

        // extends from RPCAddress
        result.setHost(serviceEntry.getHost());
        result.setPort(serviceEntry.getPort());
        result.setWeight(serviceEntry.getWeight());

        return result;
    }
}
