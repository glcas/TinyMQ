package ind.sac.mq.common.service.shutdown.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.service.invoke.InvokeService;
import ind.sac.mq.common.service.shutdown.Destroyable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientShutdownHook extends AbstractShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ClientShutdownHook.class);

    private final InvokeService invokeService;

    private final Destroyable destroyable;

    private final long waitMsForRemainReq;

    public ClientShutdownHook(InvokeService invokeService, Destroyable destroyable, long waitMsForRemainReq) {
        this.invokeService = invokeService;
        this.destroyable = destroyable;
        this.waitMsForRemainReq = waitMsForRemainReq;
    }

    @Override
    protected void doShutdown() throws InterruptedException, JsonProcessingException {

        // 在停机前等待一会尚未收到响应的请求
        long startTime = System.currentTimeMillis();
        while (invokeService.remainsRequest()) {
            if (System.currentTimeMillis() - startTime > waitMsForRemainReq) {
                logger.warn("Waiting for remaining requests timeout, force shutdown.");
                break;
            }
            Thread.sleep(10);
        }

        destroyable.destroy();
        destroyable.setEnableStatus(false);
        logger.info("Shutdown.");
    }
}
