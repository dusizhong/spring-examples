package com.dusizhong.examples.user.repository;

import com.dusizhong.examples.user.entity.ComArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComAreaRepository extends JpaRepository<ComArea, Integer> {
    List<ComArea> findALLByAreaTypeOrderBySortId(String areaType);

    ComArea findByAreaCode(String areaCode);
}
