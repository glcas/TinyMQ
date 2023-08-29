package ind.sac.mq.broker.model.bo;

import io.netty.channel.Channel;

public class GroupNameChannel {

    private String consumerGroupName;

    private Channel channel;

    public static GroupNameChannel of(String consumerGroupName, Channel channel) {
        GroupNameChannel groupNameChannel = new GroupNameChannel();
        groupNameChannel.setConsumerGroupName(consumerGroupName);
        groupNameChannel.setChannel(channel);
        return groupNameChannel;
    }

    public String getConsumerGroupName() {
        return consumerGroupName;
    }

    public void setConsumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
