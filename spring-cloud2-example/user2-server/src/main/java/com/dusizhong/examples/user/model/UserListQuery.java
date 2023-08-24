package com.dusizhong.examples.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListQuery {

    /** 用户账号 */
    private String username;
    /** 用户角色 */
    private String roles;
    /** 用户手机 */
    private String phone;
    /** 单位ids */
    private String enterpriseIds;
    /** 单位名称 */
    private String enterpriseName;
    /** 单位状态 */
    private String enterpriseStatus;
    /** 是否启用（0否 1是） */
    private Boolean enabled;

    private Integer pageNumber;
    private Integer pageSize;
}
