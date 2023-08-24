package com.dusizhong.examples.user.controller;

import com.dusizhong.examples.user.entity.ComArea;
import com.dusizhong.examples.user.entity.SysGroupArea;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.ComAreaRepository;
import com.dusizhong.examples.user.repository.SysGroupAreaRepo;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/group/area")
public class SysGroupAreaController {

    @Autowired
    private ComAreaRepository comAreaRepository;
    @Autowired
    private SysGroupAreaRepo sysGroupAreaRepo;

    @PostMapping("/save")
    public Resp save(@RequestBody SysGroupArea post) {
        if(StringUtils.isEmpty(post.getGroupId())) return Resp.error("组id不能为空");
        if(StringUtils.isEmpty(post.getAreaCode())) return Resp.error("区域代码不能为空");
        ComArea comArea = comAreaRepository.findByAreaCode(post.getAreaCode());
        if(ObjectUtils.isEmpty(comArea)) return Resp.error("区域代码无效");
        SysGroupArea exist = sysGroupAreaRepo.findByGroupIdAndAreaCode(post.getGroupId(), post.getAreaCode());
        if(!ObjectUtils.isEmpty(exist)) return Resp.error("此区域已添加过");
        if(!StringUtils.isEmpty(comArea.getAreaParent())) {
            SysGroupArea existParent = sysGroupAreaRepo.findByGroupIdAndAreaCode(post.getGroupId(), comArea.getAreaParent());
            if(!ObjectUtils.isEmpty(existParent)) return Resp.error("上级区域已添加，无须添加再添加此区域");
        }
        post.setId(SqlUtils.createId());
        post.setAreaType(comArea.getAreaType());
        post.setAreaParent(comArea.getAreaParent());
        post.setAreaName(comArea.getAreaName());
        post.setCreateUser(Oauth2Utils.getCurrentUser().getString("id"));
        post.setCreateTime(SqlUtils.getDateTime());
        return Resp.success(sysGroupAreaRepo.save(post));
    }

    @PostMapping("/list")
    public Resp list(@RequestBody SysGroupArea post) {
        if(StringUtils.isEmpty(post.getGroupId())) return Resp.error("组id不能为空");
        List<ComArea> comAreaList = comAreaRepository.findAll();
        List<SysGroupArea> sysGroupAreaList = sysGroupAreaRepo.findAllByGroupIdOrderByCreateTimeDesc(post.getGroupId());
        for(SysGroupArea sysGroupArea: sysGroupAreaList) {
            if(!StringUtils.isEmpty(sysGroupArea.getAreaParent())) {
                List<ComArea> parentList = comAreaList.stream().filter(c -> c.getAreaCode().equals(sysGroupArea.getAreaParent())).collect(Collectors.toList());
                if(parentList.size() > 0) {
                    ComArea parent = parentList.get(0);
                    sysGroupArea.setAreaName(parent.getAreaName() + "," + sysGroupArea.getAreaName());
                    if(!StringUtils.isEmpty(parent.getAreaParent())) {
                        List<ComArea> grandParentList = comAreaList.stream().filter(c -> c.getAreaCode().equals(parent.getAreaParent())).collect(Collectors.toList());
                        if(grandParentList.size() > 0) {
                            sysGroupArea.setAreaName(grandParentList.get(0).getAreaName() + "," + sysGroupArea.getAreaName());
                        }
                    }
                }
            }
        }
        return Resp.success(sysGroupAreaList);
    }

    @PostMapping("/detail")
    public Resp detail(@RequestBody SysGroupArea post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        SysGroupArea sysGroupArea = sysGroupAreaRepo.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysGroupArea)) return Resp.error("id无效");
        return Resp.success(sysGroupArea);
    }

    @PostMapping("/delete")
    public Resp delete(@RequestBody SysGroupArea post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        SysGroupArea sysGroupArea = sysGroupAreaRepo.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysGroupArea)) return Resp.error("id无效");
        if(!sysGroupArea.getCreateUser().equals(Oauth2Utils.getCurrentUser().getString("id"))) return Resp.error("创建人不符，无权操作");
        sysGroupAreaRepo.delete(sysGroupArea);
        return Resp.success();
    }
}
