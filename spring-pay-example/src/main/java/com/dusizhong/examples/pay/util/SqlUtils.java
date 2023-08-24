package com.dusizhong.examples.pay.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class SqlUtils {

    public static String createId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
