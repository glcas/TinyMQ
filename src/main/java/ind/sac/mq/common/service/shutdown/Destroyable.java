package ind.sac.mq.common.service.shutdown;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Destroyable {

    void destroy() throws JsonProcessingException;

    boolean enable();

    void setEnableStatus(boolean status);
}
