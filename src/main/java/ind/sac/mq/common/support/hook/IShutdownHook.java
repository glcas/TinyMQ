package ind.sac.mq.common.support.hook;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IShutdownHook {
    void shutdown() throws InterruptedException, JsonProcessingException;
}
