package com.dusizhong.examples.jpa.dao;

import com.dusizhong.examples.jpa.entity.SysEnterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysEnterpriseRepository extends JpaRepository<SysEnterprise, String>, JpaSpecificationExecutor<SysEnterprise> {
    SysEnterprise findByEnterpriseName(String enterpriseName);
    SysEnterprise findByEnterpriseCode(String enterpriseCode);
}
