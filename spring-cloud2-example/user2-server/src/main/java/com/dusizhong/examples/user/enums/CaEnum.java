package com.dusizhong.examples.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaEnum {

    HEBCA("hebca", "河北CA"),
    ANHCA("anhca", "安徽CA"),
    NIXCA("nixca", "宁夏CA");

    private final String code;
    private final String name;
}
