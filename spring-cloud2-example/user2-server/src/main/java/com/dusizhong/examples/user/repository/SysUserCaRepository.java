package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysUserCa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysUserCaRepository extends JpaRepository<SysUserCa, String> {
    SysUserCa findByCaTypeAndCaKey(String caType, String caKey);
    SysUserCa findByCaTypeAndSerialNumber(String certType, String serialNumber);
    SysUserCa findByUserIdAndCaType(String userId, String caType);
}
