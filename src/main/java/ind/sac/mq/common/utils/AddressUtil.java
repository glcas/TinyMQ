package ind.sac.mq.common.utils;

import ind.sac.mq.common.rpc.RPCAddress;

import java.util.ArrayList;
import java.util.List;

public class AddressUtil {

    /**
     * @param rawStr 形为"ip:port:weight,..."的字符串
     * @return 包通过解析rawStr得到的地址对象列表
     */
    public static List<RPCAddress> splitAddrFromStr(String rawStr) {
        if (rawStr.isEmpty()) {
            throw new IllegalArgumentException("Address can not be empty!");
        }
        String[] addresses = rawStr.split(",");
        List<RPCAddress> addressList = new ArrayList<>();
        for (String address :
                addresses) {
            String[] addrInfos = address.split(":");
            RPCAddress rpcAddress = new RPCAddress(addrInfos[0], Integer.parseInt(addrInfos[1]));
            if (addrInfos.length > 2) {
                rpcAddress.setWeight(Integer.parseInt(addrInfos[2]));
            }
            addressList.add(rpcAddress);
        }
        return addressList;
    }
}
