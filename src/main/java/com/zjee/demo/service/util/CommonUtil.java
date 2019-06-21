package com.zjee.demo.service.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Date: 2019-06-21 13:53
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

@Slf4j
public class CommonUtil {

    public static String readStreamToString(InputStream stream) {
        try {
            if (stream == null || stream.available() <= 0)
                return "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return "";
    }
}
