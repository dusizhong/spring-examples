package com.dusizhong.examples.user.util.tonglian;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 统一支付Post
 */
@Data
public class PayPost {

    private String bizUserId; //加密后用户id
    private String timestamp; //时间戳，格式：yyMMddHHmmss
    private String sign; //签名

    @NotNull
    private String platformCode; //平台编号（101 E招冀成、102 河北成套）
    @NotNull
    private String orderNo; //订单号订单号（30位）（生成规则：3位平台编号 + 17位时间戳（yyyyMMddHHmmssSSS）+ 10位随机数字）
    @NotNull
    private String orderType; //订单类型（BSF标书费 、PTF平台费、ZZF增值服务费、BHF保函费、HYF会员费）
    @NotNull
    private String payType; //支付方式（WX 微信 ZFB 支付宝 B2C 个人网银 B2B 企业网银 XX 线下付款 ）
    @NotNull
    private String payAmount; //金额（元）

    private String memo; //备注
}
