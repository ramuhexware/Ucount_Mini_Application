package com.freddieapp.auth.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String formatCurrentTime() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
