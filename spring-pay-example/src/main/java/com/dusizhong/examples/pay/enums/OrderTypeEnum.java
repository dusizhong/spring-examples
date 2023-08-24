package com.dusizhong.examples.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    PTF("PTF", "平台费"),
    FWF("FWF", "服务费");

    private final String code;
    private final String name;

    public static Boolean contains(String code) {
        boolean result = false;
        for(OrderTypeEnum t : OrderTypeEnum.values()) {
            if (t.getCode().equals(code)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static String getName(String code) {
        String name = "";
        for(OrderTypeEnum t : OrderTypeEnum.values()) {
            if (t.getCode().equals(code)) {
                name = t.getName();
                break;
            }
        }
        return name;
    }
}
