package com.dusizhong.examples.log.repository;

import com.dusizhong.examples.log.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysUserRepository extends JpaRepository<SysUser, String> {
    SysUser findByUsername(String username);
}
