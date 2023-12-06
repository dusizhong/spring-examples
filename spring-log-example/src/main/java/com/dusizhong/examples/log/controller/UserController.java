package com.dusizhong.examples.log.controller;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.log.component.SysLoggable;
import com.dusizhong.examples.log.entity.SysUser;
import com.dusizhong.examples.log.model.BaseResp;
import com.dusizhong.examples.log.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    @Qualifier("consumerTokenServices")
    private ConsumerTokenServices consumerTokenServices;

    @SysLoggable
    @PostMapping("/create")
    public BaseResp create(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getUsername())) return BaseResp.error("用户名不能为空");
        SysUser sysUser = sysUserRepository.findByUsername(post.getUsername());
        if(!ObjectUtils.isEmpty(sysUser)) return BaseResp.error("用户名已存在");
        post.setId(UUID.randomUUID().toString().replace("-", ""));
        post.setPassword("123");
        post.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return BaseResp.success(sysUserRepository.save(post));
    }

    @PostMapping("/update")
    public BaseResp update(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        sysUser.setUsername(post.getUsername());
        return BaseResp.success(sysUserRepository.save(sysUser));
    }

    @PostMapping("/list")
    public BaseResp list(@RequestBody SysUser post) {
        Example<SysUser> example = Example.of(post);
        return BaseResp.success(sysUserRepository.findAll(example));
    }

    @PostMapping("/detail")
    public BaseResp detail(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        return BaseResp.success(sysUser);
    }

    @PostMapping("/delete")
    public BaseResp delete(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        sysUserRepository.delete(sysUser);
        return BaseResp.success(sysUser);
    }

    @PostMapping("/oauth/logout")
    public BaseResp logout() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details == null) return BaseResp.error("获取token失败");
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
        consumerTokenServices.revokeToken(jsonObject.getString("tokenValue"));
        return BaseResp.success();
    }

    @GetMapping("/principal")
    public Principal principal(Principal principal) {
        return principal;
    }
}
