package com.dusizhong.examples.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {

    NEW("NEW", "新增"),
    EDIT("EDIT", "编辑中"),
    SUBMIT("SUBMIT", "待审核"),
    REJECT("REJECT", "审核不通过"),
    TRANSFER("TRANSFER", "转审核"),
    APPROVAL("APPROVAL", "审核通过");

    private final String code;
    private final String name;
}
