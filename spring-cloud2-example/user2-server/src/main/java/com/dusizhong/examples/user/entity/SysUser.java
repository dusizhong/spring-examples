package com.dusizhong.examples.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * 用户表
 * @author Dusizhong
 * @since 2022-09-21
 */
@Data
@Entity
public class SysUser extends BaseEntity {

    /** 用户名 */
    private String username;
    /** 密码 */
    @JsonIgnore
    private String password;
    /** 角色 */
    private String role;
    /** 手机号 */
    private String phone;
    /** 身份证 */
    private String idCardNo;
    /** 微信 */
    private String openId;
    /** 姓名 */
    private String name;
    /** 头像 */
    private String avatar;
    /** 个人信息id */
    private String userInfoId;
    /** 单位id */
    private String enterpriseId;
    /** 权限组id */
    private String groupId;

    /** 密钥是否过期 */
    private Boolean credentialsNonExpired;
    /** 账号是否过期 */
    private Boolean accountNonExpired;
    /** 账号是否锁定 */
    private Boolean accountNonLocked;
    /** 是否启用 */
    private Boolean enabled;

    @Transient
    private String sessionId;
    @Transient
    private String picCode;
    @Transient
    private String smsCode;
    @Transient
    private String oldPassword;
    @Transient
    private String newPassword;
}
