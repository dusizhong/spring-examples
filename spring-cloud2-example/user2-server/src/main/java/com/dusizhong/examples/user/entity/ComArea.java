package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 区域表
 * @author Dusizhong
 * @since 2022-09-21
 */
@Data
@Entity
public class ComArea implements Serializable {

    @Id
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
