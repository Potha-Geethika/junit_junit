package com.carbo.activitylog.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class CommonUtils {

    public static ZoneId resolveTimeZone(HttpServletRequest request) {
        String tz = request.getHeader("Time-Zone");
        if (StringUtils.hasText(tz)) {
            try {
                return ZoneId.of(tz);
            } catch (Exception e) {
                return ZoneId.of("UTC");
            }
        }
        return ZoneId.of("UTC");
    }

    public static Double round(Double number, int decimalPlaces) {
        double pow = Math.pow(10, decimalPlaces);
        return Math.round(number * pow) / pow;
    }

    public static String formatMillisToHHmm(long millis) {
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
