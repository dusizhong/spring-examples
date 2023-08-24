package com.dusizhong.examples.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {

    ROLE_SUPER("ROLE_SUPER", "超级管理员"),
    ROLE_ADMIN("ROLE_ADMIN", "普通管理员"),
    ROLE_MANAGER("ROLE_MANAGER", "客户经理"),
    ROLE_TENDEREE("ROLE_TENDEREE", "招标人"),
    ROLE_AGENCY("ROLE_AGENCY", "招标代理"),
    ROLE_BIDDER("ROLE_BIDDER", "投标人"),
    ROLE_AUDITOR("ROLE_AUDITOR", "资审人"),
    ROLE_SUPERVISOR("ROLE_SUPERVISOR", "监督人"),
    ROLE_EXPERT("ROLE_EXPERT", "评标专家"),
    ROLE_SERVICE("ROLE_SERVICE", "客服"),
    ROLE_FINANCE("ROLE_FINANCE", "财务人员"),
    ROLE_USER("ROLE_USER", "普通用户");

    private final String code;
    private final String name;
}
