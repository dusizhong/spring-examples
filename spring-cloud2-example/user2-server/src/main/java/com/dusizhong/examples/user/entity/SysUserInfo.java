package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.List;

@Data
@Entity
public class SysUserInfo extends BaseEntity {

    private String userId;
//    private String userType;

    /** 姓名 */
    private String name;
    private String sex;
    private String birthday;
    private String education;
    private String school;
    private String major;
    private String phone;
    private String email;
    private String address;
    private String idCardNo;

    private String idCardFrontPic;
    private String idCardBackPic;

    //专家信息
    @Transient
    private String expertType; //专家类型
    @Transient
    private String isProvince; //是否省级评委
    @Transient
    private String registerArea; //专家注册省市区
    @Transient
    private String evalMajor; //评标专业
    @Transient
    private String enterpriseName; //所在单位
    @Transient
    private String enterpriseCode; //所在单位统一代码
    @Transient
    private String isPosition; //是否在职
    @Transient
    private String positionName; //在职职位
    @Transient
    private String technicalTitle; //技术职称
    @Transient
    private String qualificationSequence; //专家职业资格序列
    @Transient
    private String qualificationLevel; //专家职业资格等级
    @Transient
    private String expertExperience; //专家从业经历
    @Transient
    private String expertWorkingYears; //专家从业年限

    @Transient
    private String role;
    @Transient
    private List<SysUserInfoMaterial> materialList;
}
