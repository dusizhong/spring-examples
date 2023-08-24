package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysEnterpriseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysEnterpriseMaterialRepository extends JpaRepository<SysEnterpriseMaterial, String> {

    SysEnterpriseMaterial findByEnterpriseIdAndMaterialType(String enterpriseId, String materialType);
    List<SysEnterpriseMaterial> findAllByEnterpriseId(String enterpriseId);
}
