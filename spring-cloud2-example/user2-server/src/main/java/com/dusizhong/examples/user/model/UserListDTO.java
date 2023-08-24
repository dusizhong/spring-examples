package com.dusizhong.examples.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {

    /** 用户id */
    private String id;
    /** 用户账号 */
    private String username;
    /** 用户角色 */
    private String role;
    /** 用户手机 */
    private String phone;
    /** 用户姓名 */
    private String name;
    /** 用户头像 */
    private String avatar;
    /** 权限组id */
    private String groupId;
    /** 单位id */
    private String enterpriseId;
    /** 是否启用（0否 1是） */
    private Boolean enabled;
    /** 单位代码 */
    private String enterpriseCode;
    /** 单位名称 */
    private String enterpriseName;
    /** 单位状态 */
    private String enterpriseStatus;
}
