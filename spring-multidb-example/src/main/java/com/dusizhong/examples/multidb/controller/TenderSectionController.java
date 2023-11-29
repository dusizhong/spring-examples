package com.dusizhong.examples.multidb.controller;

import com.dusizhong.examples.multidb.entity.tender.TenderSection;
import com.dusizhong.examples.multidb.repository.tender.TenderSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tenderSection")
public class TenderSectionController {

    @Autowired
    private TenderSectionRepository tenderSectionRepository;

    @RequestMapping("/list")
    public List<TenderSection> list(@RequestBody TenderSection post) {
        return tenderSectionRepository.findAll();
    }
}
