package ind.sac.mq.common.dto.request;

import java.util.List;
import java.util.Objects;

public class MQMessage extends MQCommonRequest {

    private String topic;

    private List<String> tags;

    private byte[] payload;

    /**
     * 业务标识
     */
    private String bizKey;

    private String shardingKey;

    public MQMessage() {
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getBizKey() {
        return this.bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public String getShardingKey() {
        return this.shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MQMessage)) return false;
        final MQMessage other = (MQMessage) o;
        if (!other.canEqual(this)) return false;
        final Object this$topic = this.getTopic();
        final Object other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        final Object this$tags = this.getTags();
        final Object other$tags = other.getTags();
        if (!Objects.equals(this$tags, other$tags)) return false;
        if (!java.util.Arrays.equals(this.getPayload(), other.getPayload())) return false;
        final Object this$bizKey = this.getBizKey();
        final Object other$bizKey = other.getBizKey();
        if (!Objects.equals(this$bizKey, other$bizKey)) return false;
        final Object this$shardingKey = this.getShardingKey();
        final Object other$shardingKey = other.getShardingKey();
        return Objects.equals(this$shardingKey, other$shardingKey);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MQMessage;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $topic = this.getTopic();
        result = result * PRIME + ($topic == null ? 43 : $topic.hashCode());
        final Object $tags = this.getTags();
        result = result * PRIME + ($tags == null ? 43 : $tags.hashCode());
        result = result * PRIME + java.util.Arrays.hashCode(this.getPayload());
        final Object $bizKey = this.getBizKey();
        result = result * PRIME + ($bizKey == null ? 43 : $bizKey.hashCode());
        final Object $shardingKey = this.getShardingKey();
        result = result * PRIME + ($shardingKey == null ? 43 : $shardingKey.hashCode());
        return result;
    }

    public String toString() {
        return "MQMessage(topic=" + this.getTopic() + ", tags=" + this.getTags() + ", payload=" + java.util.Arrays.toString(this.getPayload()) + ", bizKey=" + this.getBizKey() + ", shardingKey=" + this.getShardingKey() + ")";
    }
}
