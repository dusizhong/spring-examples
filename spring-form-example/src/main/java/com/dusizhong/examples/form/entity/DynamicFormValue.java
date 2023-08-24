package com.dusizhong.examples.form.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class DynamicFormValue {

    @Id
    @GeneratedValue
    private Integer id;
    private Integer userId;
    private Integer formId;
    private Integer fieldId;
    private String fieldValue;
    private String createTime;
}
