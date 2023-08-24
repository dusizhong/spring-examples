package com.dusizhong.examples.user.controller;

import com.dusizhong.examples.user.entity.SysGroup;
import com.dusizhong.examples.user.entity.SysGroupArea;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.SysGroupAreaRepo;
import com.dusizhong.examples.user.repository.SysGroupRepo;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.PageHelper;
import com.dusizhong.examples.user.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/group")
public class SysGroupController {

    @Autowired
    private SysGroupRepo sysGroupRepo;
    @Autowired
    private SysGroupAreaRepo sysGroupAreaRepo;
    @Autowired
    private SysUserRepository sysUserRepository;

    @PostMapping("/save")
    public Resp save(@RequestBody SysGroup post) {
        if(StringUtils.isEmpty(post.getGroupName())) return Resp.error("组名称不能为空");
        SysGroup existName = sysGroupRepo.findByGroupName(post.getGroupName());
        if(StringUtils.isEmpty(post.getId())) {
            if(!ObjectUtils.isEmpty(existName)) return Resp.error("组名称已存在");
            post.setId(SqlUtils.createId());
            post.setCreateUser(Oauth2Utils.getCurrentUser().getString("id"));
            post.setCreateTime(SqlUtils.getDateTime());
        } else {
            if(!ObjectUtils.isEmpty(existName) && !existName.getId().equals(post.getId())) return Resp.error("组名称已存在");
            SysGroup sysGroup = sysGroupRepo.findById(post.getId()).orElse(null);
            if(ObjectUtils.isEmpty(sysGroup)) return Resp.error("id无效");
            if(!sysGroup.getCreateUser().equals(Oauth2Utils.getCurrentUser().getString("id"))) return Resp.error("创建人不符，无权操作");
            post.setCreateUser(sysGroup.getCreateUser());
            post.setCreateTime(sysGroup.getCreateTime());
        }
        return Resp.success(sysGroupRepo.save(post));
    }

    @PostMapping("/list")
    public Resp list(@RequestBody SysGroup post) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("groupName", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<SysGroup> example = Example.of(post, matcher);
        Page<SysGroup> pageData = sysGroupRepo.findAll(example, PageHelper.of(post.getPageNumber(), post.getPageSize()));
//        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(pageData);
//        jsonObject.put("pageNumber", jsonObject.getIntValue("number") + 1);
        return Resp.success(pageData);
    }

    @PostMapping("/detail")
    public Resp detail(@RequestBody SysGroup post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        SysGroup sysGroup = sysGroupRepo.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysGroup)) return Resp.error("id无效");
        return Resp.success(sysGroup);
    }

    @Transactional
    @PostMapping("/delete")
    public Resp delete(@RequestBody SysGroup post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        SysGroup sysGroup = sysGroupRepo.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysGroup)) return Resp.error("id无效");
        if(!sysGroup.getCreateUser().equals(Oauth2Utils.getCurrentUser().getString("id"))) return Resp.error("创建人不符，无权操作");
        sysGroupRepo.delete(sysGroup);
        List<SysGroupArea> sysGroupAreaList = sysGroupAreaRepo.findAllByGroupId(post.getId());
        sysGroupAreaRepo.deleteAll(sysGroupAreaList);
        List<SysUser> sysUserList = sysUserRepository.findByGroupId(post.getId());
        for(SysUser sysUser : sysUserList) {
            sysUser.setGroupId(null);
        }
        sysUserRepository.saveAll(sysUserList);
        return Resp.success();
    }
}
