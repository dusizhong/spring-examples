package com.dusizhong.examples.util.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SqlUtils {

    public static String createId() {
        return UUIDUtils.createUUID();
    }

    public static String getDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
