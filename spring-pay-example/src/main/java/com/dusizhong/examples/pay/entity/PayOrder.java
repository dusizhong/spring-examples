package com.dusizhong.examples.pay.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

@Data
@Entity
public class PayOrder implements Serializable {

    @Id
    private String id;
    /** 订单号 */
    private String orderNo;
    /** 订单类型（FWF服务费 PTF平台费）*/
    private String orderType;
    /** 支付方式（WX微信 ZFB支付宝 YL银联 B2C个人网银 B2B企业网银 XX线下支付） */
    private String payType;
    /** 支付金额（元）*/
    private String payAmount;
    /** 支付手续费（内扣）*/
    private String payFee;
    /** 支付发起时间 */
    private String payTime;

    /** 付款人id */
    private String payerId;
    /** 付款人代码 */
    private String payerCode;
    /** 付款人名称 */
    private String payerName;
    /** 付款人ip */
    private String payerIp;

    /** 收款方id */
    private String receiverId;
    /** 收款方代码 */
    private String receiverCode;
    /** 收款方名称 */
    private String receiverName;

    /** 支付通道名称 */
    private String channelName;
    /** 支付通道商户号（收银宝商户号） */
    private String channelMchId;
    /** 支付通道交易单号 */
    private String channelTransId;
    /** 支付通道支付人账号（微信用户openid、支付宝用户id、银行卡号） */
    private String channelPayerId;
    /** 支付通道交易完成时间 */
    private String channelPayTime;

    /** 业务id */
    private String bizId;
    /** 业务代码 */
    private String bizCode;
    /** 业务名称 */
    private String bizName;

    /** 订单过期时间 */
    private String expiredTime;
    /** 摘要 */
    private String memo;
    /** 是否已申请发票（0未申请 1已申请 2已开票） */
    private String isInvoiced;
    /** 申请发票记录id */
    private String invoiceRecordId;

    private String status;
    private String remark;
    private String updateUser;
    private String updateTime;
    @Column(updatable = false)
    private String createUser;
    @Column(updatable = false)
    private String createTime;

    @Transient
    private Integer pageNumber;
    @Transient
    private Integer pageSize;

    /** 支付二维码（WX ZFB YL）*/
    @Transient
    private String payQrCode;
    /** 支付跳转地址（B2B B2C）*/
    @Transient
    private String payUrl;
    /** 前台通知地址 */
    @Transient
    private String frontUrl;
    /** 后台通知地址 */
    @Transient
    private String backUrl;

    /** 付款时间段查询 */
    @Transient
    private String startTime;
    @Transient
    private String endTime;
}
