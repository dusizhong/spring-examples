package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@Data
public class SysGroupArea implements Serializable {

    @Id
    private String id;
    private String groupId;
    private String areaType;
    private String areaParent;
    private String areaCode;
    private String areaName;
    private String createUser;
    private String createTime;
}
