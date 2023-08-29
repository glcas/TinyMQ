package ind.sac.mq.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import ind.sac.mq.common.rpc.RPCMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class DelimiterUtil {

    public static final String DELIMITER = "~!@#$%^&*";

    /**
     * length is necessary in case of avoiding buffer overflow.
     */
    public static final int LENGTH = 65536;

    public static final ByteBuf DELIMITER_BUF = Unpooled.copiedBuffer(DELIMITER.getBytes());

    public static ByteBuf getByteBuf(String text) {
        return Unpooled.copiedBuffer(text.getBytes());
    }

    public static ByteBuf getRPCMessageWithDelimiterBuffer(RPCMessage rpcMessage) throws JsonProcessingException {
        String jsonStr = JsonUtil.writeAsJsonString(rpcMessage);
        String delimitedJSON = jsonStr + DELIMITER;
        return Unpooled.copiedBuffer(delimitedJSON.getBytes());
    }
}
