package ind.sac.mq.common.support;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Destroyable {

    void destroyAll() throws JsonProcessingException;

    boolean enable();

    void setEnableStatus(boolean status);
}
