package com.dusizhong.examples.cloud.user.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Entity
public class CommArea implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;
    private Integer sortId;
    private String areaType;
    private String areaParent;
    private String areaCode;
    private String areaName;
    private String areaAlias;
    private String areaLnt;
    private String areaLat;
    private String updateTime;
    private String createTime;
}
