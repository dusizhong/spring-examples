package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysApprovalRecordRepository extends JpaRepository<SysApprovalRecord, String> {
}
