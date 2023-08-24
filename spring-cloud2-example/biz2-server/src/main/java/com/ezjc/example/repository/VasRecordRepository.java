package com.ezjc.example.repository;

import com.ezjc.example.entity.VasRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VasRecordRepository extends JpaRepository<VasRecord, String> {
}
