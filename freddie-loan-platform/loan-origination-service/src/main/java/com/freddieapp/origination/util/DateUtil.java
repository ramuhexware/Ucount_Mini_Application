package com.freddieapp.origination.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String formatISO(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_FORMATTER) : "";
    }
}
