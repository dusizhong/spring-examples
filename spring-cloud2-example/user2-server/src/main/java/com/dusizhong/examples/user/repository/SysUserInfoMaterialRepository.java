package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysUserInfoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysUserInfoMaterialRepository extends JpaRepository<SysUserInfoMaterial, String> {
    SysUserInfoMaterial findByUserIdAndMaterialType(String userId, String materialType);
    List<SysUserInfoMaterial> findByUserId(String userId);
}
