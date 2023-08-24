package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.SysGroupArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysGroupAreaRepo extends JpaRepository<SysGroupArea, String> {
    SysGroupArea findByGroupIdAndAreaCode(String groupId, String areaCode);
    List<SysGroupArea> findAllByGroupId(String groupId);

    List<SysGroupArea> findAllByGroupIdOrderByCreateTimeDesc(String groupId);
}
