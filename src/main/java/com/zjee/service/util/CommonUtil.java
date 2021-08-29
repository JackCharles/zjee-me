package com.zjee.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

/**
 * Date: 2019-06-21 13:53
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

@Slf4j
public class CommonUtil {

    public static final long KB = 1204L;

    public static final long MB = 1024L * 1024L;

    public static final long GB = 1024L * 1024L * 1024L;

    public static String readStreamToString(InputStream stream) {
        try {
            if (stream == null) {
                return "";
            }
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

    public static String getLanguage(String str) {
        if (StringUtils.isEmpty(str)) {
            return "en";
        }
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) > 255) {
                return "zh";
            }
        }
        return "en";
    }

    public static String formatByteUnit(long byteLen) {
        if (byteLen > GB) {
            return String.format("%.2fGB", (byteLen * 1.0) / GB);
        } else if (byteLen > MB) {
            return String.format("%.2fMB", (byteLen * 1.0) / MB);
        }
        if (byteLen > KB) {
            return String.format("%.2fKB", (byteLen * 1.0) / KB);
        }
        return String.format("%dB", byteLen);
    }

    public static double toDouble(Object object) {
        try {
            return Double.parseDouble(String.valueOf(object));
        } catch (Exception e) {
            log.error("parse double error: ", e);
        }
        return 0d;
    }

    public static String doubleToPercent(double d) {
        return String.format("%.2f%%", d * 100.0);
    }

    public static double round(double d, int digital) {
        BigDecimal bg = new BigDecimal(d);
        return bg.setScale(digital, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
