package ind.sac.mq.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseJson(String json, Class<T> classType) throws JsonProcessingException {
        return objectMapper.readValue(json, classType);
    }

    public static <T> T parseJson(byte[] json, Class<T> classType) throws IOException {
        return objectMapper.readValue(json, classType);
    }

    public static String writeAsJsonString(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }
}
