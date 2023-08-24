package com.dusizhong.examples.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.enums.RoleEnum;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.PageHelper;
import com.dusizhong.examples.user.util.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 代理用户管理
 * @author Dusizhong
 * @since 2023-02-13
 */
@Slf4j
@RestController
@RequestMapping("/agency/user")
public class SysAgencyUserController {

    @Autowired
    private SysUserRepository sysUserRepository;

    @PostMapping("/save")
    public Resp save(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getUsername())) return Resp.error("用户名不能为空");
        if(StringUtils.isEmpty(post.getNewPassword())) return Resp.error("密码不能为空");
        SysUser sysUser = new SysUser();
        SysUser exist = sysUserRepository.findByUsername(post.getUsername());
        if(StringUtils.isEmpty(post.getId())) {
            sysUser.setId(SqlUtils.createId());
            if(!ObjectUtils.isEmpty(exist)) return Resp.error("用户名已存在");
        } else {
            sysUser = sysUserRepository.findById(post.getId()).orElse(null);
            if (ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
            if(!sysUser.getEnterpriseId().equals(Oauth2Utils.getCurrentUser().getString("enterpriseId"))) return Resp.error("无权操作其他单位信息");
            if(!sysUser.getId().equals(exist.getId())) return Resp.error("用户名已存在");
        }
        sysUser.setUsername(post.getUsername());
        sysUser.setPassword(new BCryptPasswordEncoder().encode(post.getNewPassword()));
        sysUser.setRole(RoleEnum.ROLE_AGENCY.getCode());
        sysUser.setAccountNonExpired(true);
        sysUser.setAccountNonLocked(true);
        sysUser.setCredentialsNonExpired(true);
        sysUser.setEnabled(true);
        sysUser.setEnterpriseId(Oauth2Utils.getCurrentUser().getString("enterpriseId"));
        return Resp.success(sysUserRepository.save(sysUser));
    }

    @RequestMapping(value = "/update/password")
    public Resp updatePassword(@RequestBody @Valid SysUser post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        if(StringUtils.isEmpty(post.getNewPassword())) return Resp.error("新密码不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if (ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
        if(!sysUser.getEnterpriseId().equals(Oauth2Utils.getCurrentUser().getString("enterpriseId"))) return Resp.error("无权操作其他单位信息");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        sysUser.setPassword(bCryptPasswordEncoder.encode(post.getNewPassword()));
        return Resp.success(sysUserRepository.save(sysUser));
    }

    @PostMapping("/list")
    public Resp list(@RequestBody SysUser post) {
        post.setEnterpriseId(Oauth2Utils.getCurrentUser().getString("enterpriseId"));
        Example<SysUser> example = Example.of(post);
        return Resp.success(sysUserRepository.findAll(example, PageHelper.of(post.getPageNumber(), post.getPageSize())));
    }

    @PostMapping("/detail")
    public Resp detail(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if (ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
        return Resp.success(sysUser);
    }

    @PostMapping("/delete")
    public Resp delete(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if (ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
        if(!sysUser.getEnterpriseId().equals(Oauth2Utils.getCurrentUser().getString("enterpriseId"))) return Resp.error("无权操作其他单位信息");
        sysUserRepository.deleteById(post.getId());
        log.info("DELETE: {}", JSONObject.toJSONString(sysUser, SerializerFeature.WriteMapNullValue));
       return Resp.success();
    }
}
