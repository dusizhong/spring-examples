package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysGroupRepo extends JpaRepository<SysGroup, String> {
    SysGroup findByGroupName(String groupName);
}
