package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * CA证书表
 * @author Dusizhong
 * @since 2022-09-21
 */
@Data
@Entity
public class SysUserCa extends BaseEntity {

    /** 所属用户id */
    private String userId;
    /** 证书类型（hebca、anhca、nixca） */
    private String caType;
    /** 证书唯一项 */
    private String caKey;
    /** 证书序列号 */
    private String serialNumber;
    /** 证书主体 */
    private String subject;
    /** 证书颁发者 */
    private String issuer;
    /** 证书生效时间 */
    private String beginTime;
    /** 证书失效时间 */
    private String endTime;
    /** 证书签名算法 */
    private String algorithm;
    /** 证书 */
    private String cert;

    /** 登录随机数 */
    @Transient
    private String random;
    /** 签名 */
    @Transient
    private String sign;
}
