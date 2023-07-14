package ind.sac.mq.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class MqMessage {

    private String topic;

    private List<String> tags;

    private byte[] payload;

    /**
     * 业务标识
     */
    private String bizKey;

    private String shardingKey;

}
