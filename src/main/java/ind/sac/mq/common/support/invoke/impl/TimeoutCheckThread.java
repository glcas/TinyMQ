package ind.sac.mq.common.support.invoke.impl;

import ind.sac.mq.common.rpc.RPCMessageDTO;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此方法避免有请求发出后一直被挂起重试，导致内存浪费
 */
public class TimeoutCheckThread implements Runnable {

    private final ConcurrentHashMap<String, Long> requestMap;
    private final ConcurrentHashMap<String, RPCMessageDTO> responseMap;

    public TimeoutCheckThread(@NotNull ConcurrentHashMap<String, Long> requestMap, ConcurrentHashMap<String, RPCMessageDTO> responseMap) {
        this.requestMap = requestMap;
        this.responseMap = responseMap;
    }

    @Override
    public void run() {
        for (Map.Entry<String, Long> entry : requestMap.entrySet()) {
            long expireTime = entry.getValue();
            long currentTime = System.currentTimeMillis();
            if (currentTime > expireTime) {
                final String key = entry.getKey();
                responseMap.putIfAbsent(key, RPCMessageDTO.timeout());
                requestMap.remove(key);
            }
        }
    }
}
