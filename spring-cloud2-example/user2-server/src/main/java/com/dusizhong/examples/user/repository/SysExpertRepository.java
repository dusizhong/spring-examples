package com.dusizhong.examples.user.repository;


import com.dusizhong.examples.user.entity.SysExpert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysExpertRepository extends JpaRepository<SysExpert, String> {
    SysExpert findByUserId(String userId);
    SysExpert findByExpertIdCardNo(String expertIdCardNo);
}
