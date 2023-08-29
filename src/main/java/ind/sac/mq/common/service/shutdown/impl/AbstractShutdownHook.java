package ind.sac.mq.common.service.shutdown.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.service.shutdown.ShutdownHook;

public abstract class AbstractShutdownHook implements ShutdownHook {
    @Override
    public void shutdown() throws InterruptedException, JsonProcessingException {
        this.doShutdown();
    }

    protected abstract void doShutdown() throws InterruptedException, JsonProcessingException;
}


