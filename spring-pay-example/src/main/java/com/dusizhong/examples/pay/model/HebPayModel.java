package com.dusizhong.examples.pay.model;

public class HebPayModel {

    private String weixinQrUrl;
    private String alipayQrUrl;
    private String unionpayQrUrl;

    public HebPayModel() {}

    public String getWeixinQrUrl() {
        return weixinQrUrl;
    }

    public void setWeixinQrUrl(String weixinQrUrl) {
        this.weixinQrUrl = weixinQrUrl;
    }

    public String getAlipayQrUrl() {
        return alipayQrUrl;
    }

    public void setAlipayQrUrl(String alipayQrUrl) {
        this.alipayQrUrl = alipayQrUrl;
    }

    public String getUnionpayQrUrl() {
        return unionpayQrUrl;
    }

    public void setUnionpayQrUrl(String unionpayQrUrl) {
        this.unionpayQrUrl = unionpayQrUrl;
    }
}
