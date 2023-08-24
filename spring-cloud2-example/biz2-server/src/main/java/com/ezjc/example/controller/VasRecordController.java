package com.ezjc.example.controller;

import com.alibaba.fastjson.JSONObject;
import com.ezjc.example.entity.VasRecord;
import com.ezjc.example.model.Resp;
import com.ezjc.example.repository.VasRecordRepository;
import com.ezjc.example.service.SysEnterpriseService;
import com.ezjc.example.util.Oauth2Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/vas")
public class VasRecordController {

    @Autowired
    private VasRecordRepository vasRecordRepository;
    @Autowired
    private SysEnterpriseService sysEnterpriseService;

    @PostMapping("/create")
    public Resp create(@RequestBody @Valid VasRecord post) {
        if(StringUtils.isEmpty(post.getContactPerson())) return Resp.error("联系人不能为空");
        //获取用户
        JSONObject currentUser = Oauth2Utils.getCurrentUser();
        System.out.println(currentUser);
        //获取单位
        JSONObject enterprise = sysEnterpriseService.findById(currentUser.getString("enterpriseId"));
        if(ObjectUtils.isEmpty(enterprise)) return Resp.error("获取单位信息失败");
        //新增单位
        post.setId(UUID.randomUUID().toString());
        post.setEnterpriseId(enterprise.getString("id"));
        post.setEnterpriseName(enterprise.getString("enterpriseName"));
        post.setExpiredTime(LocalDateTime.now().plusDays(365).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        post.setStatus("EDIT");
        post.setCreateUser(currentUser.getString("id"));
        post.setCreateTime(LocalDateTime.now().toString());
        return Resp.success(vasRecordRepository.save(post));
    }

    @RequestMapping("/list")
    public Page<VasRecord> list(@RequestBody @Valid VasRecord query) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("enterpriseName", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<VasRecord> example = Example.of(query, matcher);
        Pageable pageable = PageRequest.of(0, 20);
        return vasRecordRepository.findAll(example, pageable);
    }

    @PostMapping("/detail")
    public Resp detail(@RequestBody @Valid VasRecord post) {
        return Resp.success(vasRecordRepository.findById(post.getId()));
    }

    @PostMapping("/delete")
    public Resp delete(@RequestBody @Valid VasRecord post) {
        vasRecordRepository.deleteById(post.getId());
        return Resp.success();
    }
}
