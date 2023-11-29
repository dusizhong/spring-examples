package com.dusizhong.examples.multidb.repository.user;

import com.dusizhong.examples.multidb.entity.user.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysUserRepository extends JpaRepository<SysUser, String> {
}
