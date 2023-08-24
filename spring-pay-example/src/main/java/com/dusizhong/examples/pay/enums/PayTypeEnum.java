package com.dusizhong.examples.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayTypeEnum {

    WX("WX", "微信支付"),
    ZFB("ZFB", "支付宝"),
    YL("YL", "银联支付"),
    B2B("B2B", "企业网银"),
    B2C("B2C", "个人网银"),
    BANK("BANK", "线下银行汇款"),
    QRCODE("QRCODE", "线下二维码");

    private final String code;
    private final String desc;

    public static Boolean contains(String code) {
        boolean result = false;
        for(PayTypeEnum t : PayTypeEnum.values()) {
            if (t.getCode().equals(code)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
