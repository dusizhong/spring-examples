package com.dusizhong.examples.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListVO {

    private String id;
    private String username;
    private String role;
    private String phone;
    private String avatar;
    private Boolean enabled;
    private String createTime;
    private String enterpriseId;
    private String enterpriseName;
    private String enterpriseCode;
    private String enterpriseRole;
    private String enterpriseStatus;
}
