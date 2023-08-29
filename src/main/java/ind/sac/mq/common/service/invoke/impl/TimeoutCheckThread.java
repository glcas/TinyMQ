package ind.sac.mq.common.service.invoke.impl;

import ind.sac.mq.common.rpc.RPCMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此方法避免有请求发出后一直被挂起重试，导致CPU空占；以及请求/响应通信过程中丢失后的内存浪费
 */
public class TimeoutCheckThread implements Runnable {

    private final ConcurrentHashMap<Long, Long> requestMap;
    private final ConcurrentHashMap<Long, RPCMessage> responseMap;

    public TimeoutCheckThread(@NotNull ConcurrentHashMap<Long, Long> requestMap, ConcurrentHashMap<Long, RPCMessage> responseMap) {
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
                responseMap.putIfAbsent(key, RPCMessage.timeout());
                requestMap.remove(key);
            }
        }
    }
}
