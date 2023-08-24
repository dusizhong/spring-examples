package com.dusizhong.examples.cloud.user.model;

import lombok.Data;

@Data
public class UserListVO {

    private String id;
    private String username;
    private String role;
    private String phone;
    private String avatar;
    private String enabled;
    private String createTime;
    private String enterpriseId;
    private String enterpriseName;
    private String enterpriseCode;
    private String enterpriseRole;
    private String enterpriseStatus;
}
