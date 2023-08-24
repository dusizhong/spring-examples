package com.dusizhong.examples.cloud.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {

    ROLE_ADMIN("ROLE_ADMIN", "管理员"),
    ROLE_MANAGER("ROLE_MANAGER", "经理"),
    ROLE_USER("ROLE_USER", "用户");

    private final String code;
    private final String name;
}
