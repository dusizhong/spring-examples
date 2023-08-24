package com.dusizhong.examples.user.model;

import lombok.Data;

import java.util.List;

@Data
public class TreeModel {

    private String code;
    private String name;
    private List<TreeModel> children;
}