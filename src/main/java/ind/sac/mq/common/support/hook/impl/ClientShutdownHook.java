package ind.sac.mq.common.support.hook.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.support.Destroyable;
import ind.sac.mq.common.support.invoke.IInvokeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientShutdownHook extends AbstractShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ClientShutdownHook.class);

    private final IInvokeService invokeService;

    private final Destroyable destroyable;

    private final long waitMsForRemainReq;

    public ClientShutdownHook(IInvokeService invokeService, Destroyable destroyable, long waitMsForRemainReq) {
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

        destroyable.destroyAll();
        destroyable.setEnableStatus(false);
        logger.info("Shutdown.");
    }
}
