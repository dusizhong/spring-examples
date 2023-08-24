package com.dusizhong.examples.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.entity.SysExpert;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.enums.RoleEnum;
import com.dusizhong.examples.user.enums.StatusEnum;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.SysExpertRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.service.CaService;
import com.dusizhong.examples.user.util.MD5Utils;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.PageHelper;
import com.dusizhong.examples.user.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 专家接口
 * @author Dusizhong
 * @since 2022-10-20
 */
@RestController
@RequestMapping("/expert")
public class SysExpertController {

    @Autowired
    private SysExpertRepository sysExpertRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private CaService caService;

    @PostMapping("/list")
    public Resp list(@RequestBody SysExpert post) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("expertIdCardNo", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("expertName", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<SysExpert> example = Example.of(post, matcher);
        Page<SysExpert> pageData = sysExpertRepository.findAll(example, PageHelper.of(post.getPageNumber()-1, post.getPageSize()));
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(pageData);
        jsonObject.put("pageNumber", jsonObject.getIntValue("number") + 1);
        return Resp.success(jsonObject);
    }

    /**
     * 新增专家用户
     * 用于评标代理系统添加专家
     * @param post
     * @return
     */
    @Transactional
    @PostMapping("/createExpertUser")
    public Resp createUserExpert(@RequestBody SysExpert post) {
        if(StringUtils.isEmpty(post.getExpertName())) return Resp.error("专家姓名不能为空");
        if(StringUtils.isEmpty(post.getExpertRole())) return Resp.error("专家角色不能为空");
        if(StringUtils.isEmpty(post.getExpertIdCardNo())) return Resp.error("专家身份证号不能为空");
        if(post.getExpertIdCardNo().length() != 18) return Resp.error("专家身份证号应为18位");
        if(!RoleEnum.ROLE_EXPERT.getCode().equals(post.getExpertRole()) &&
                !RoleEnum.ROLE_AUDITOR.getCode().equals(post.getExpertRole()) &&
                !RoleEnum.ROLE_SUPERVISOR.getCode().equals(post.getExpertRole())) return Resp.error("角色值无效");
        SysExpert existExpertIdCardNo = sysExpertRepository.findByExpertIdCardNo(post.getExpertIdCardNo());
        if(!ObjectUtils.isEmpty(existExpertIdCardNo)) return Resp.error("专家库中身份证号已存在");
        SysUser existUserIdCardNo = sysUserRepository.findByIdCardNo(post.getExpertIdCardNo());
        if(!ObjectUtils.isEmpty(existUserIdCardNo)) return Resp.error("用户库中身份证号已存在");
        SysUser existUsername = sysUserRepository.findByUsername(post.getExpertIdCardNo());
        if(!ObjectUtils.isEmpty(existUsername)) return Resp.error("用户库中此身份证号的用户名已存在");
        SysUser sysUser = new SysUser();
        sysUser.setId(SqlUtils.createId());
        sysUser.setUsername(post.getExpertIdCardNo());
        String password = MD5Utils.encrypt32(post.getExpertIdCardNo().substring(post.getExpertIdCardNo().length() - 6));
        sysUser.setPassword(new BCryptPasswordEncoder().encode(password));
        sysUser.setRole(post.getExpertRole());
        sysUser.setName(post.getExpertName());
        sysUser.setIdCardNo(post.getExpertIdCardNo());
        sysUser.setAccountNonExpired(true);
        sysUser.setAccountNonLocked(true);
        sysUser.setCredentialsNonExpired(true);
        sysUser.setEnabled(true);
        sysUser.setStatus(StatusEnum.EDIT.getCode());
        sysUserRepository.save(sysUser);
        post.setId(SqlUtils.createId());
        post.setUserId(sysUser.getId());
        post.setIsRegCa("0");
        post.setStatus(StatusEnum.EDIT.getCode());
        sysExpertRepository.save(post);
        return Resp.success(post);
    }

    /**
     * 更新专家用户
     * 用于专家首次登录评标系统自行完善信息（手机号等）
     * @param post
     * @return
     */
    @Transactional
    @PostMapping("/updateExpertUser")
    public Resp updateExpertUser(@RequestBody SysExpert post) {
        if(StringUtils.isEmpty(post.getUserId())) return Resp.error("用户id不能为空");
        if(StringUtils.isEmpty(post.getExpertName())) return Resp.error("专家姓名不能为空");
        if(StringUtils.isEmpty(post.getExpertPhone())) return Resp.error("专家手机号不能为空");
        if(!post.getExpertPhone().startsWith("1") || post.getExpertPhone().length()!= 11) return Resp.error("专家手机号格式无效");
        SysExpert sysExpert = sysExpertRepository.findByUserId(post.getUserId());
        if(ObjectUtils.isEmpty(sysExpert)) return Resp.error("异常！专家信息不存在");
        if(!Oauth2Utils.getCurrentUser().getString("id").equals(sysExpert.getUserId())) return Resp.error("非专家本人，无权修改");
        post.setId(sysExpert.getId());
        post.setUserId(sysExpert.getUserId());
        post.setExpertRole(sysExpert.getExpertRole());
        post.setExpertIdCardNo(sysExpert.getExpertIdCardNo()); //身份证号创建账号，暂不能修改
        post.setIsRegCa(sysExpert.getIsRegCa());
        sysExpert = sysExpertRepository.save(post);
        //todo: 20230726 暂时去除，同步到用户库需要验证手机号是否重复 (暂未更新到正式)
//        SysUser sysUser = sysUserRepository.findById(post.getUserId()).orElse(null);
//        if(ObjectUtils.isEmpty(sysUser)) return Resp.error("异常！同步更新用户信息时未获取到用户信息");
//        sysUser.setName(post.getExpertName());
//        sysUser.setPhone(post.getExpertPhone());
//        sysUserRepository.save(sysUser);
        return Resp.success(sysExpert);
    }

    /**
     * 删除专家用户
     * 评标代理系统删除专家用户
     * @param post
     * @return
     */
    @Transactional
    @PostMapping("/deleteExpertUser")
    public Resp deleteExpertUser(@RequestBody SysExpert post) {
        if(StringUtils.isEmpty(post.getUserId())) return Resp.error("用户id不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getUserId()).orElse(null);
        if(ObjectUtils.isEmpty(sysUser)) return Resp.error("用户id无效");
        if(!StatusEnum.EDIT.getCode().equals(sysUser.getStatus())) return Resp.error("用户不是编辑状态，不能操作");
        SysExpert sysExpert = sysExpertRepository.findByUserId(post.getUserId());
        if(ObjectUtils.isEmpty(sysExpert)) return Resp.error("专家信息不存在");
        sysExpertRepository.delete(sysExpert);
        sysUserRepository.delete(sysUser);
        return Resp.success();
    }

    /**
     * 注册CA云签章
     * @param post
     * @return
     */
    @PostMapping("/regCa")
    public Resp regCa(@RequestBody SysExpert post) {
        if(StringUtils.isEmpty(post.getUserId())) return Resp.error("用户id不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getUserId()).orElse(null);
        if(ObjectUtils.isEmpty(sysUser)) return Resp.error("用户id无效");
        SysExpert sysExpert = sysExpertRepository.findByUserId(post.getUserId());
        if(ObjectUtils.isEmpty(sysExpert)) return Resp.error("异常！专家信息不存在");
        if(StringUtils.isEmpty(sysExpert.getExpertName())) return Resp.error("异常！专家信息中姓名为空");
        if(StringUtils.isEmpty(sysExpert.getExpertPhone())) return Resp.error("异常！专家信息中手机号为空");
        if(StringUtils.isEmpty(sysExpert.getExpertIdCardNo())) return Resp.error("异常！专家信息中身份证号码为空");
        JSONObject resp = caService.receiveExpert(sysExpert);
        if(!resp.getInteger("code").equals(0)) return Resp.error(resp.getString("message"));
        //todo: 20230726 暂时去除，同步到用户库需要验证手机号、身份证号是否重复 (暂未更新到正式)
//        sysUser.setName(sysExpert.getExpertName());
//        sysUser.setPhone(sysExpert.getExpertPhone());
//        sysUser.setIdCardNo(sysExpert.getExpertIdCardNo());
//        sysUser.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
//        sysUser.setUpdateTime(SqlUtils.getDateTime());
//        sysUserRepository.save(sysUser);
        sysExpert.setIsRegCa("1");
        sysExpertRepository.save(sysExpert);
        return Resp.success("http://47.93.149.252:9191/h5");
    }
}
