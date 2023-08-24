package com.dusizhong.examples.form.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Data
@Entity
public class DynamicFormField {

    @Id
    @GeneratedValue
    private Integer id;
    private Integer formId;
    private Integer sortId;
    private String fieldName;
    private String fieldType;
    private String fieldNote;
    private String updateTime;
    private String createTime;

    @Transient
    private String fieldValue;
}
