package com.dusizhong.examples.cloud.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Data
@Entity
public class SysUser extends BaseEntity {

    private String username;
    @JsonIgnore
    private String password;
    private String role;
    private String phone;
    private String idCardNo;
    private String avatar;
    private String enterpriseId;
    private Boolean credentialsNonExpired;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
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
