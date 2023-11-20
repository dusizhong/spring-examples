package com.dusizhong.examples.form.model;

import lombok.Data;

import java.util.List;

@Data
public class BaseTree {

    private String code;
    private String name;
    private List<BaseTree> children;
}
