package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * 单位表
 * @author Dusizhong
 * @since 2022-09-22
 */
@Data
@Entity
public class SysEnterprise extends BaseEntity {

    /** 序列id（用于生成单位附件目录？） */
    private String sid;
    /** 单位名称 */
    private String enterpriseName;
    /** 统一社会信用代码 */
    private String enterpriseCode;
    /** 单位角色 */
    private String enterpriseRole;
    /** 国别地区（中国、台湾、香港、澳门） */
    private String enterpriseNation;
    /** 所属省市区代码 */
    private String enterpriseArea;
    /** 所属省市区中文 */
    private String enterpriseAreaCn;
    /** 所在地址 */
    private String enterpriseAddress;
    /** 单位性质 */
    private String enterpriseNature;
    /** 行业类型 */
    private String enterpriseIndustry;
    /** 资信等级 */
    private String enterpriseCredit;
    /** 成立日期 */
    private String enterpriseEstablish;
    /** 经营期限 */
    private String enterpriseTerm;
    /** 经营范围 */
    private String enterpriseRange;
    /** 注册资本(万元) */
    private String enterpriseCapital;
    /** 注册币种 */
    private String enterpriseCapitalCurrency;

    /** 法人类型 */
    private String legalType;
    /** 法人名称 */
    private String legalName;
    /** 法人身份证号 */
    private String legalIdCardNo;
    /** 法人手机号 */
    private String legalPhone;

    /** 开户总行 */
    private String bankTitle;
    /** 开户行 */
    private String bankName;
    /** 开户行代码 */
    private String bankCode;
    /** 开户行账号 */
    private String bankAccountNo;

    /** 联系人 */
    private String contactName;
    /** 联系电话 */
    private String contactPhone;
    /** 联系邮箱 */
    private String contactEmail;
    /** 联系地址 */
    private String contactAddress;

    /** 注册责任人 */
    private String registerUser;
    /** 注册责任人电话 */
    private String registerPhone;
    /** 注册责任人地址 */
    private String registerAddress;

    /** 审核人 */
    private String approvalUser;
    /** 审核时间 */
    private String approvalTime;
    /** 审核记录id */
    private String approvalRecordId;

    /** 营业执照电子件 */
    private String enterpriseLicensePic;
    /** 法人身份证电子件 */
    private String legalIdCardPic;
    /** 注册人身份证电子件 */
    private String registerIdCardPic;
    /** 法人授权书电子件 */
    private String legalAuthorizePic;

    /** 注册账号的电话 */
    @Transient
    private String registPhone;

//    @Transient
//    private List<SysEnterpriseMaterial> materialList;
}
