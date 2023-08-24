package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class ComIndustry {

    @Id
    private Integer id;
    private Integer sortId;
    private String industryParent;
    private String industryCode;
    private String industryName;
    private String industryLevel;
    private String createTime;
}
