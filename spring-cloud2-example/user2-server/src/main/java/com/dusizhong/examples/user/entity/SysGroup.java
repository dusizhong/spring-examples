package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

@Data
@Entity
public class SysGroup implements Serializable {

    @Id
    private String id;
    private String groupName;
    private String groupDesc;
    private String createUser;
    private String createTime;

    @Transient
    private Integer pageNumber;
    @Transient
    private Integer pageSize;
}
