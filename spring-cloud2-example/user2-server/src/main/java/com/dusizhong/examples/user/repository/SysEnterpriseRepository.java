package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysEnterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysEnterpriseRepository extends JpaRepository<SysEnterprise, String>, JpaSpecificationExecutor<SysEnterprise> {

    SysEnterprise findByEnterpriseName(String enterpriseName);
    SysEnterprise findByEnterpriseCode(String enterpriseCode);
}
