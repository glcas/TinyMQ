package ind.sac.mq.producer.dto;

import ind.sac.mq.producer.constant.SendStatus;
import lombok.Data;

@Data
public class SendResult {

    /**
     * Unique Id
     */
    private String messageId;

    private SendStatus status;

}
