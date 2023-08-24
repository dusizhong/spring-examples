package com.ezjc.example.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
public class VasRecord implements Serializable {

    @Id
    private String id;

    /** 购买单位id */
    private String enterpriseId;

    /** 购买单位名称 */
    private String enterpriseName;

    /** 服务到期时间 */
    private String expiredTime;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 状态（EXPIRED已过期） */
    private String status;

    /** 备注 */
    private String remark;

    /** 更新人 */
    private String updateUser;

    /** 更新时间 */
    private Date updateTime;

    /** 创建人 */
    private String createUser;

    /** 创建时间 */
    private String createTime;
}

