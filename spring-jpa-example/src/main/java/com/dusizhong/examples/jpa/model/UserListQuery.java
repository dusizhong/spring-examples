package com.dusizhong.examples.jpa.model;

import lombok.Data;

@Data
public class UserListQuery {

    private String username;
    private String role;
    private String phone;
    private String enterpriseId;
    private String enterpriseName;
    private String enterpriseCode;
    private String enterpriseStatus;
    private Boolean enabled;

    private Integer pageNumber;
    private Integer pageSize;
}
