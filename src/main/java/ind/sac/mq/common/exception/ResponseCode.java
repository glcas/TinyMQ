package ind.sac.mq.common.exception;

import java.io.Serializable;

/**
 * 错误响应码接口定义，包含代码和描述的get方法
 */
public interface ResponseCode extends Serializable {

    String getCode();

    String getDescription();

}
