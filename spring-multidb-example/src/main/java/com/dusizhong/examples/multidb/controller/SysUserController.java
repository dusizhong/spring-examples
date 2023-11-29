package com.dusizhong.examples.multidb.controller;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.multidb.entity.user.SysUser;
import com.dusizhong.examples.multidb.repository.user.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserRepository sysUserRepository;

    @RequestMapping("/list")
    public List<SysUser> list(@RequestBody SysUser post) {
        return sysUserRepository.findAll();
    }
}
