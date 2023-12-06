package com.dusizhong.examples.log.repository;

import com.dusizhong.examples.log.entity.SysLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysLogRepository extends JpaRepository<SysLog, Long> {
}
