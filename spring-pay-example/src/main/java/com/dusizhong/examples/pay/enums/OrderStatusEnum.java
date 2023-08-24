package com.dusizhong.examples.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING("PENDING", "发起支付"),
    SUCCESS("SUCCESS", "支付成功"),
    FAIL("FAIL", "支付失败"),
    REFUND("REFUND", "已退款");

    private final String code;
    private final String desc;

    public static Boolean contains(String code) {
        boolean result = false;
        for(OrderStatusEnum t : OrderStatusEnum.values()) {
            if (t.getCode().equals(code)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
