package com.dusizhong.examples.multidb.repository.tender;

import com.dusizhong.examples.multidb.entity.tender.TenderSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderSectionRepository extends JpaRepository<TenderSection, String> {
}
