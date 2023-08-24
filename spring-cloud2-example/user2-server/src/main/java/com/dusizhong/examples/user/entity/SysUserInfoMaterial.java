package com.dusizhong.examples.user.entity;

import lombok.Data;

import javax.persistence.Entity;


/**
 * 用户附件表
 * @author Dusizhong
 * @since 2022-09-26
 */
@Data
@Entity
public class SysUserInfoMaterial extends BaseEntity {

    /** 所属用户id */
    private String userId;
    /** 附件类型 */
    private String materialType;
    /** 文件名称 */
    private String fileName;
    /** 文件别名 */
    private String fileAlias;
    /** 文件类型 */
    private String fileType;
    /** 文件大小 */
    private Long fileSize;
    /** 文件path */
    private String filePath;
    /** 文件url */
    private String fileUrl;
}
