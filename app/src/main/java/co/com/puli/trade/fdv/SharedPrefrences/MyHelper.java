package co.com.puli.trade.fdv.SharedPrefrences;

import java.util.Map;

public class MyHelper {

    public static final String BOUNDARY = "login_boundry";

    public static String createPostBody(Map<String, String> params)
    {
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            if (params.get(key) != null) {
                sb.append("\r\n" + "--" + BOUNDARY + "\r\n");
                sb.append("Content-Disposition: form-data; name=\""
                        + key + "\"" + "\r\n\r\n");
                sb.append(params.get(key));
            }
        }

        return sb.toString();
    }
}
