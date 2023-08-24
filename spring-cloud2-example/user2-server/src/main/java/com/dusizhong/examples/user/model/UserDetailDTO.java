package com.dusizhong.examples.user.model;

import com.dusizhong.examples.user.entity.SysGroupArea;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户详情DTO
 * @author Dusizhong
 * @since 2022-11-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO implements Serializable {

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
    /** CA锁名称 */
    private String caSubject;
    /** CA锁有效期 */
    private String caEndTime;
    /** 单位id */
    private String enterpriseId;
    /** 单位代码 */
    private String enterpriseCode;
    /** 单位名称 */
    private String enterpriseName;
    /** 单位状态 */
    private String enterpriseStatus;

    /** 用户组区域 */
    private List<SysGroupArea> groupArea;
}
