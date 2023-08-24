package com.dusizhong.examples.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.entity.ComArea;
import com.dusizhong.examples.user.entity.ComIndustry;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.enums.RoleEnum;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.model.TreeModel;
import com.dusizhong.examples.user.repository.ComAreaRepository;
import com.dusizhong.examples.user.repository.ComIndustryRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.service.CaptchaService;
import com.dusizhong.examples.user.util.SqlUtils;
import com.dusizhong.examples.user.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 公共接口
 * @author Dusizhong
 * @since 2022-09-21
 */
@RestController
public class IndexController {

    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private ComAreaRepository comAreaRepository;
    @Autowired
    private ComIndustryRepository comIndustryRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    @Qualifier("consumerTokenServices")
    private ConsumerTokenServices consumerTokenServices;

    @RequestMapping("/")
    public String greetings() {
        return "Greetings from User Server";
    }

    @RequestMapping("/captcha/generate")
    public Resp generateCaptcha(HttpServletRequest request) {
        //todo: 增加接口防刷机制
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", request.getSession().getId());
        jsonObject.put("picImg", captchaService.generateCaptcha(request.getSession().getId()));
        return Resp.success(jsonObject);
    }

    //@CrossOrigin //不通过网关访问时开启，用于测试
    @PostMapping("/sms/send")
    public Resp sendSmsCode(HttpServletRequest request, @RequestBody SysUser post) {
        System.out.println(request.getSession().getId()); //谷歌浏览器每次sessionId不一致，不能用
        //todo: 增加接口防刷机制
        if(StringUtils.isEmpty(post.getPhone())) return Resp.error("手机号不能为空");
        if(!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return Resp.error("手机号无效");
        if(StringUtils.isEmpty(post.getSessionId())) return Resp.error("sessionId不能为空");
        if(StringUtils.isEmpty(post.getPicCode())) return Resp.error("图片验证码不能为空");
        if(!captchaService.verifyCaptcha(post.getSessionId(), post.getPicCode())) return Resp.error("图片验证码无效");
        if(!smsService.sendSmsCode(post.getPhone())) return Resp.error("验证码已发送，请在手机上查看");
        return Resp.success(SmsService.EXPIRED_SECONDS);
    }

    @PostMapping("/area/list")
    public Resp areaList(@RequestBody ComArea post) {
        Example<ComArea> example = Example.of(post);
        return Resp.success(comAreaRepository.findAll(example, Sort.by("sortId")));
    }

    @PostMapping("/area/tree")
    public Resp areaTree() {
        List<ComArea> provinceList = comAreaRepository.findALLByAreaTypeOrderBySortId("0");
        List<ComArea> cityList = comAreaRepository.findALLByAreaTypeOrderBySortId("1");
        List<ComArea> areaList = comAreaRepository.findALLByAreaTypeOrderBySortId("2");
        List<TreeModel> resultTree = new ArrayList<>();
        for(ComArea province : provinceList) {
            TreeModel provinceTree = new TreeModel();
            provinceTree.setCode(province.getAreaCode());
            provinceTree.setName(province.getAreaName());
            List<TreeModel> provinceChildren = new ArrayList<>();
            for(ComArea city : cityList) {
                if(city.getAreaParent().equals(province.getAreaCode())) {
                    TreeModel cityTree = new TreeModel();
                    cityTree.setCode(city.getAreaCode());
                    cityTree.setName(city.getAreaName());
                    List<TreeModel> cityChildren = new ArrayList<>();
                    for(ComArea area : areaList) {
                        if(area.getAreaParent().equals(city.getAreaCode())) {
                            TreeModel areaTree = new TreeModel();
                            areaTree.setCode(area.getAreaCode());
                            areaTree.setName(area.getAreaName());
                            areaTree.setChildren(null);
                            cityChildren.add(areaTree);
                        }
                    }
                    cityTree.setChildren(cityChildren);
                    provinceChildren.add(cityTree);
                }
            }
            provinceTree.setChildren(provinceChildren);
            resultTree.add(provinceTree);
        }
        return Resp.success(resultTree);
    }

    @PostMapping("/industry/tree")
    public Resp industryTree() {
        List<ComIndustry> industryList = comIndustryRepository.findAll();
        List<TreeModel> resultTree = new ArrayList<>();
        for (ComIndustry industry : industryList) {
            if (ObjectUtils.isEmpty(industry.getIndustryParent())) {
                TreeModel treeModel = new TreeModel();
                treeModel.setCode(industry.getIndustryCode());
                treeModel.setName(industry.getIndustryName());
                //children1
                List<TreeModel> children1 = new ArrayList<>();
                for (ComIndustry industry1 : industryList) {
                    if (treeModel.getCode().equals(industry1.getIndustryParent())) {
                        TreeModel child1 = new TreeModel();
                        child1.setCode(industry1.getIndustryCode());
                        child1.setName(industry1.getIndustryName());
                        //children2
                        List<TreeModel> children2 = new ArrayList<>();
                        for (ComIndustry industry2 : industryList) {
                            if (child1.getCode().equals(industry2.getIndustryParent())) {
                                TreeModel child2 = new TreeModel();
                                child2.setCode(industry2.getIndustryCode());
                                child2.setName(industry2.getIndustryName());
                                //children3
                                List<TreeModel> children3 = new ArrayList<>();
                                for (ComIndustry industry3 : industryList) {
                                    if (child1.getCode().equals(industry3.getIndustryParent())) {
                                        TreeModel child3 = new TreeModel();
                                        child3.setCode(industry3.getIndustryCode());
                                        child3.setName(industry3.getIndustryName());
                                        child3.setChildren(null);
                                        children3.add(child3);
                                    }
                                }
                                child2.setChildren(children3);
                                children2.add(child2);
                            }
                        }
                        child1.setChildren(children2);
                        children1.add(child1);
                    }
                }
                treeModel.setChildren(children1);
                resultTree.add(treeModel);
            }
        }
        return Resp.success(resultTree);
    }

    @PostMapping("/register")
    public Resp register(@RequestBody @Valid SysUser post) {
        if(StringUtils.isEmpty(post.getUsername())) return Resp.error("用户名不能为空");
        if(StringUtils.isEmpty(post.getNewPassword())) return Resp.error("密码不能为空");
        if(StringUtils.isEmpty(post.getRole())) return Resp.error("角色不能为空");
        if(Arrays.stream(RoleEnum.values()).noneMatch(r -> r.name().equals(post.getRole()))) return Resp.error("角色值无效");
        if(StringUtils.isEmpty(post.getPhone())) return Resp.error("手机号不能为空");
        if(!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return Resp.error("手机号无效");
        if(StringUtils.isEmpty(post.getSmsCode())) return Resp.error("短信验证码不能为空");
        SysUser existUserName = sysUserRepository.findByUsername(post.getUsername());
        if(existUserName != null) return Resp.error("用户名已存在");
        SysUser existPhone = sysUserRepository.findByPhone(post.getPhone());
        if(existPhone != null) return Resp.error("手机号已存在");
        if(!smsService.verifySmsCode(post.getPhone(), post.getSmsCode())) return Resp.error("短信验证码无效");
        SysUser sysUser = new SysUser();
        sysUser.setId(SqlUtils.createId());
        sysUser.setUsername(post.getUsername());
        sysUser.setPassword(new BCryptPasswordEncoder().encode(post.getNewPassword()));
        sysUser.setRole(post.getRole());
        sysUser.setPhone(post.getPhone());
        sysUser.setAccountNonExpired(true);
        sysUser.setAccountNonLocked(true);
        sysUser.setCredentialsNonExpired(true);
        sysUser.setEnabled(true);
        return Resp.success(sysUserRepository.save(sysUser));
    }

    @PostMapping("/resetPassword")
    public Resp resetPassword(@RequestBody @Valid SysUser post) {
        if(StringUtils.isEmpty(post.getPhone())) return Resp.error("手机号不能为空");
        if(!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return Resp.error("手机号无效");
        if(StringUtils.isEmpty(post.getSmsCode())) return Resp.error("短信验证码不能为空");
        if(StringUtils.isEmpty(post.getNewPassword())) return Resp.error("新密码不能为空");
        SysUser sysUser = sysUserRepository.findByPhone(post.getPhone());
        if(sysUser == null) return Resp.error("手机号不存在");
        if(!smsService.verifySmsCode(post.getPhone(), post.getSmsCode())) return Resp.error("短信验证码无效");
        sysUser.setPassword(new BCryptPasswordEncoder().encode(post.getNewPassword()));
        return Resp.success(sysUserRepository.save(sysUser));
    }

    @PostMapping("/oauth/logout")
    public Resp logout() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details == null) return Resp.error("获取token失败");
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
        consumerTokenServices.revokeToken(jsonObject.getString("tokenValue"));
        return Resp.success();
    }

    //资源服务器统一鉴权接口 user_info_token的地址
    @GetMapping("/principal")
    public Principal principal(Principal principal) {
        return principal;
    }
}