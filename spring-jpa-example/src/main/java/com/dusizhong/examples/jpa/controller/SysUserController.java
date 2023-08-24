package com.dusizhong.examples.jpa.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dusizhong.examples.jpa.dao.SysUserRepository;
import com.dusizhong.examples.jpa.entity.SysUser;
import com.dusizhong.examples.jpa.model.*;
import com.dusizhong.examples.jpa.util.PageHelper;
import com.dusizhong.examples.jpa.util.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Value("${spring.resources.static-locations}")
    private String STATIC_PATH;
    @Value("${resources.static-url}")
    private String STATIC_URL;

    @Autowired
    private SysUserRepository sysUserRepository;

    @PostMapping("/create")
    public BaseResp create(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getUsername())) return BaseResp.error("用户名不能为空");
        if(StringUtils.isEmpty(post.getNewPassword())) return BaseResp.error("密码不能为空");
        post.setId(SqlUtils.createId());
        post.setPassword(post.getNewPassword());
        SysUser existUsername = sysUserRepository.findByUsername(post.getUsername());
        if(!ObjectUtils.isEmpty(existUsername)) return BaseResp.error("用户名已存在");
        if(!StringUtils.isEmpty(post.getPhone())) {
            if (!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return BaseResp.error("手机号无效");
            SysUser existPhone = sysUserRepository.findByPhone(post.getPhone());
            if (!ObjectUtils.isEmpty(existPhone)) return BaseResp.error("手机号已存在");
        }
        if(!StringUtils.isEmpty(post.getIdCardNo())) {
            if (post.getIdCardNo().length() != 18) return BaseResp.error("身份证号应为18位");
            SysUser existIdCardNo = sysUserRepository.findByIdCardNo(post.getIdCardNo());
            if (!ObjectUtils.isEmpty(existIdCardNo)) return BaseResp.error("身份证号已存在");
        }
        post.setAccountNonExpired(true);
        post.setAccountNonLocked(true);
        post.setCredentialsNonExpired(true);
        post.setEnabled(true);
        return BaseResp.success(sysUserRepository.save(post));
    }

    @PostMapping("/update")
    public BaseResp update(@RequestBody SysUser post) {
        if (StringUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findOne(post.getId());
        if (ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        if (!StringUtils.isEmpty(post.getNewPassword())) sysUser.setPassword(post.getNewPassword());
        if (!StringUtils.isEmpty(post.getPhone())) {
            if (!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return BaseResp.error("手机号无效");
            SysUser existPhone = sysUserRepository.findByPhone(post.getPhone());
            if (!ObjectUtils.isEmpty(existPhone) && !existPhone.getId().equals(sysUser.getId())) return BaseResp.error("手机号已存在");
            sysUser.setPhone(post.getPhone());
        }
        if (!StringUtils.isEmpty(post.getIdCardNo())) {
            if (post.getIdCardNo().length() != 18) return BaseResp.error("身份证号应为18位");
            SysUser existIdCardNo = sysUserRepository.findByIdCardNo(post.getIdCardNo());
            if (!ObjectUtils.isEmpty(existIdCardNo) && !existIdCardNo.getId().equals(sysUser.getId())) return BaseResp.error("身份证号已存在");
            sysUser.setIdCardNo(post.getIdCardNo());
        }
        if (!ObjectUtils.isEmpty(post.getEnabled())) sysUser.setEnabled(post.getEnabled());
        return BaseResp.success(sysUserRepository.save(sysUser));
    }

    @PostMapping("/list")
    public BaseResp list(@RequestBody UserListQuery post) {
        List<String> enterpriseIds = null;
        List<String> enterpriseStatus = null;
        if(!StringUtils.isEmpty(post.getEnterpriseId())) enterpriseIds = Arrays.asList(post.getEnterpriseId().split(","));
        if(!StringUtils.isEmpty(post.getEnterpriseStatus())) enterpriseStatus = Arrays.asList(post.getEnterpriseStatus().split(","));
        Pageable pageable = PageHelper.of(post.getPageNumber(), post.getPageSize());
        Page<UserListVO> pageData = sysUserRepository.queryList(post.getUsername(), post.getRole(), post.getPhone(), post.getEnabled(), enterpriseIds, post.getEnterpriseName(), post.getEnterpriseCode(), enterpriseStatus, pageable);
        return BaseResp.success(pageData);
    }

    @PostMapping("/list2")
    public BaseResp list2(@RequestBody SysUser post) {
        Pageable pageable = PageHelper.of(post.getPageNum(), post.getPageSize());
        Page<SysUser> pageData = sysUserRepository.findAll(pageable);
        return BaseResp.success(pageData);
    }

    @PostMapping("/detail")
    public BaseResp detail(@RequestBody SysUser post) {
        SysUser sysUser = sysUserRepository.findOne(post.getId());
        if(ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        if(!StringUtils.isEmpty(sysUser.getAvatar())) sysUser.setAvatar(STATIC_URL + sysUser.getAvatar());
        return BaseResp.success(sysUser);
    }

    @PostMapping("/delete")
    public BaseResp delete(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findOne(post.getId());
        if(ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        sysUserRepository.delete(post.getId());
        log.info("DELETE: {}", JSONObject.toJSONString(sysUser, SerializerFeature.WriteMapNullValue));
        return BaseResp.success();
    }
}
