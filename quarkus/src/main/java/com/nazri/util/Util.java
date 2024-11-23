package com.nazri.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Util {

    public static String getCurrentTime() {
        return Instant.now().atZone(ZoneId.of("Asia/Singapore")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Checks if the input string is a valid numeric value, including decimals.
     *
     * @param input the string to check
     * @return true if the string is a valid numeric value, false otherwise
     */
    public static boolean isNumeric(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
