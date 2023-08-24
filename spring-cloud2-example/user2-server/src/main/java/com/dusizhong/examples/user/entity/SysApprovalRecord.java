package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;

/**
 * 审核记录表
 * @author Dusizhong
 * @since 2022-09-23
 */
@Data
@Entity
public class SysApprovalRecord extends BaseEntity {

    /** 审核项类型（ENTERPRISE单位） */
    private String approvalItemType;
    /** 审核项id */
    private String approvalItemId;
    /** 审核项名称 */
    private String approvalItemName;
    /** 审核项url */
    private String approvalItemUrl;
    /** 提交人id */
    private String submitUserId;
    /** 提交人姓名 */
    private String submitUserName;
    /** 提交说明 */
    private String submitRemark;
    /** 提交时间 */
    private String submitTime;
    /** 审核人id */
    private String approvalUserId;
    /** 审核人姓名 */
    private String approvalUserName;
    /** 审核结果 */
    private String approvalResult;
    /** 审核时间 */
    private String approvalTime;

}
