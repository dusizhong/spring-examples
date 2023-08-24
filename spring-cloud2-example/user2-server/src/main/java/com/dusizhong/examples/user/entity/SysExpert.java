package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;

/**
 * 专家表
 * 用于评标系统新增 1专家 2资审 3监督等评标角色
 * @author Dusizhong
 * @since 2022-10-19
 */
@Data
@Entity
public class SysExpert extends BaseEntity {

    /** 用户id */
    private String userId;
    /** 专家身份证号 */
    private String expertIdCardNo;
    /** 专家姓名 */
    private String expertName;
    /** 专家手机号 */
    private String expertPhone;
    /** 专家邮箱 */
    private String expertEmail;
    /** 专家地址 */
    private String expertAddress;
    /** 专家角色 */
    private String expertRole;
    /** 是否省级评委 0否 1是 */
    private String isProvince;
    /** 评标专业 */
    private String evalMajor;
    /** 专家注册省市区代码 */
    private String registerArea;
    /** 专家注册省市区名称 */
    private String registerAreaCn;
    /** 所在单位 */
    private String enterpriseName;
    /** 所在单位代码 */
    private String enterpriseCode;
    /** 是否在职 */
    private String isPosition;
    /** 在职职位 */
    private String positionName;
    /** 技术职称 */
    private String technicalTitle;
    /** 专家职业资格序列 */
    private String qualifySequence;
    /** 专家职业资格等级 */
    private String qualifyLevel;
    /** 专家执业资质 */
    private String qualifyLicense;
    /**专家省库职业资格 */
    private String qualifyProvince;
    /** 专家从业年限 */
    private String workYear;
    /** 专家从业经历 */
    private String workExperience;

    /** 是否注册CA云签章（0否 1是） */
    private String isRegCa;
}
