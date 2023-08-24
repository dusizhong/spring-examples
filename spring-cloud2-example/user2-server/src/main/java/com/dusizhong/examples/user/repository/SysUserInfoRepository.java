package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysUserInfo;
import com.dusizhong.examples.user.entity.SysUserInfoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SysUserInfoRepository extends JpaRepository<SysUserInfo, String>, JpaSpecificationExecutor<SysUserInfo> {

    SysUserInfo findByUserId(String userId);
    SysUserInfo findByIdCardNo(String idCardNo);
    List<SysUserInfoMaterial> findAllByUserId(String userId);
}
