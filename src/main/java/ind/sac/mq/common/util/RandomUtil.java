package ind.sac.mq.common.util;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomUtil {

    public static <T> T loadBalance(final List<T> list, String key) {
        if (list.isEmpty()) {
            return null;
        }
        if (key == null || key.isEmpty()) {
            // 目前负载均衡在没有指定key下的策略就是随机选择
            Random random = new Random();
            return list.get(random.nextInt(list.size()));
        }

        // 根据传入（sharding）key的hash从list获得结果，是否负载平衡取决于key的设置
        int hashCode = Objects.hashCode(key);
        return list.get(hashCode % list.size());
    }
}
