package com.dusizhong.examples.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class SysEnterprise extends BaseEntity {

    private String enterpriseName;
    private String enterpriseCode;
    private String enterpriseRole;
    private String enterpriseType;
    private String enterpriseLegal;
    private String enterpriseCapital;
    private String enterpriseAddress;
    private String enterpriseEstablish;
    private String enterpriseTerm;
    private String enterpriseRange;

    private String enterpriseBankName;
    private String enterpriseBankCode;
    private String enterpriseBankAccountNo;

    private String registerId;
    private String registerName;
    private String registerPhone;
    private String registerAddress;

    private String licensePic;
    private String registerIdCardPic;
    private String legalIdCardPic;
    private String legalAuthorizePic;

    private String approvalRecordId;
}
