package ind.sac.mq.common.util;

import java.util.List;
import java.util.regex.Pattern;

public class RegexUtil {
    public static boolean hasMatch(String regex, List<String> strings) {
        for (String string : strings) {
            if (Pattern.compile(regex).matcher(string).find()) {
                return true;
            }
        }
        return false;
    }
}
