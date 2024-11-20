package com.nazri.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    public static String getCurrentTime() {
        return Instant.now().atZone(ZoneId.of("Asia/Singapore")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
