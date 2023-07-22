package ind.sac.mq.common.support.invoke.impl;

import ind.sac.mq.common.rpc.RPCMessageDTO;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此方法避免有请求发出后一直被挂起重试，导致内存浪费
 */
public class TimeoutCheckThread implements Runnable {

    private final ConcurrentHashMap<Long, Long> requestMap;
    private final ConcurrentHashMap<Long, RPCMessageDTO> responseMap;

    public TimeoutCheckThread(@NotNull ConcurrentHashMap<Long, Long> requestMap, ConcurrentHashMap<Long, RPCMessageDTO> responseMap) {
        this.requestMap = requestMap;
        this.responseMap = responseMap;
    }

    @Override
    public void run() {
        for (Map.Entry<Long, Long> entry : requestMap.entrySet()) {
            long expireTime = entry.getValue();
            long currentTime = System.currentTimeMillis();
            if (currentTime > expireTime) {
                final long key = entry.getKey();
                responseMap.putIfAbsent(key, RPCMessageDTO.timeout());
                requestMap.remove(key);
            }
        }
    }
}
