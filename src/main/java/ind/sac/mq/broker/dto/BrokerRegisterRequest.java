package ind.sac.mq.broker.dto;

import ind.sac.mq.broker.support.ServiceEntry;
import ind.sac.mq.common.constant.MethodType;
import ind.sac.mq.common.dto.request.MQCommonRequest;

/**
 * 此类存在的必要性是，继承了普通请求MQCommonRequest这一DTO，而这是统一控制RPC传输对象的要求
 * 而serviceEntry是被包装在请求中的具体内容，内容不能继承请求类及其中字段，这些在逻辑处理中是无用的
 */
public class BrokerRegisterRequest extends MQCommonRequest {

    private ServiceEntry serviceEntry;

    public BrokerRegisterRequest() {
    }

    public BrokerRegisterRequest(long traceId, MethodType methodType, ServiceEntry serviceEntry) {
        super(traceId, methodType);
        this.serviceEntry = serviceEntry;
    }

    public ServiceEntry getServiceEntry() {
        return serviceEntry;
    }
}
