package com.dusizhong.examples.multidb.entity.tender;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class TenderSection {

    @Id
    private String id;
    private String tenderSectionNo;
    private String tenderSectionName;
}