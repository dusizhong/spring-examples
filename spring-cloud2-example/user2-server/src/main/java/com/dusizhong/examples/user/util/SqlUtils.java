package com.dusizhong.examples.user.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SqlUtils {

    public static String createId() {
        return UUIDUtils.getUUID();
    }

    public static String getDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
