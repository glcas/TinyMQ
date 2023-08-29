package ind.sac.mq.common.service.shutdown;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ShutdownHook {
    void shutdown() throws InterruptedException, JsonProcessingException;
}
