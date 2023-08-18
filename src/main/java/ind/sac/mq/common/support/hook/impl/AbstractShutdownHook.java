package ind.sac.mq.common.support.hook.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.support.hook.IShutdownHook;

public abstract class AbstractShutdownHook implements IShutdownHook {
    @Override
    public void shutdown() throws InterruptedException, JsonProcessingException {
        this.doShutdown();
    }

    protected abstract void doShutdown() throws InterruptedException, JsonProcessingException;
}


