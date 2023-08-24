package com.dusizhong.examples.user.controller;

import com.dusizhong.examples.user.entity.SysEnterprise;
import com.dusizhong.examples.user.entity.SysGroupArea;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.entity.SysUserCa;
import com.dusizhong.examples.user.enums.RoleEnum;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.model.UserDetailDTO;
import com.dusizhong.examples.user.model.UserListDTO;
import com.dusizhong.examples.user.model.UserListQuery;
import com.dusizhong.examples.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.user.repository.SysGroupAreaRepo;
import com.dusizhong.examples.user.repository.SysUserCaRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.service.SmsService;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.PageHelper;
import com.dusizhong.examples.user.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 用户接口
 * @author Dusizhong
 * @since 2022-09-21
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Value("${spring.resources.static-locations}")
    private String RES_PATH;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserCaRepository sysUserCaRepository;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;
    @Autowired
    private SysGroupAreaRepo sysGroupAreaRepo;
    @Autowired
    private SmsService smsService;

    @PostMapping("/list")
    public Resp list(@RequestBody UserListQuery post) {
        List<String> roles = null;
        List<String> enterpriseIds = null;
        List<String> enterpriseStatus = null;
        if(!StringUtils.isEmpty(post.getRoles())) roles = Arrays.asList(post.getRoles().split(","));
        if(!StringUtils.isEmpty(post.getEnterpriseIds())) enterpriseIds = Arrays.asList(post.getEnterpriseIds().split(","));
        if(!StringUtils.isEmpty(post.getEnterpriseStatus())) enterpriseStatus = Arrays.asList(post.getEnterpriseStatus().split(","));
        Pageable pageable = PageHelper.of(post.getPageNumber(), post.getPageSize());
        Page<UserListDTO> pageData = sysUserRepository.queryList(post.getUsername(), roles, post.getPhone(), post.getEnabled(), enterpriseIds, post.getEnterpriseName(), enterpriseStatus, pageable);
        return Resp.success(pageData);
    }

    /**
     * 获取用户详情
     * @param post
     * @return
     */
    @PostMapping("/detail")
    public Resp detail(@RequestBody SysUser post) {
        System.err.println(Oauth2Utils.getCurrentUser());
        UserDetailDTO userDetailDTO = new UserDetailDTO();
        if(StringUtils.isEmpty(post.getId())) post.setId(Oauth2Utils.getCurrentUser().getString("id"));
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
        userDetailDTO.setId(sysUser.getId());
        userDetailDTO.setUsername(sysUser.getUsername());
        userDetailDTO.setRole(sysUser.getRole());
        userDetailDTO.setPhone(sysUser.getPhone());
        userDetailDTO.setName(sysUser.getName());
        userDetailDTO.setAvatar(sysUser.getAvatar());
        SysUserCa sysUserCa = sysUserCaRepository.findByUserIdAndCaType(post.getId(), "hebca");
        if(!ObjectUtils.isEmpty(sysUserCa)) {
            userDetailDTO.setCaSubject(sysUserCa.getSubject());
            userDetailDTO.setCaEndTime(sysUserCa.getEndTime());
        }
        if(!StringUtils.isEmpty(sysUser.getEnterpriseId())) {
            SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(sysUser.getEnterpriseId()).orElse(null);
            if (!ObjectUtils.isEmpty(sysEnterprise)) {
                userDetailDTO.setEnterpriseId(sysEnterprise.getId());
                userDetailDTO.setEnterpriseCode(sysEnterprise.getEnterpriseCode());
                userDetailDTO.setEnterpriseName(sysEnterprise.getEnterpriseName());
                userDetailDTO.setEnterpriseStatus(sysEnterprise.getStatus());
            }
        }
        if(!StringUtils.isEmpty(sysUser.getGroupId())) {
            List<SysGroupArea> groupAreaList = sysGroupAreaRepo.findAllByGroupId(sysUser.getGroupId());
            userDetailDTO.setGroupArea(groupAreaList);
        }
        return Resp.success(userDetailDTO);
    }

    /**
     * 上传头像
     * @param file
     * @return
     */
    @PostMapping("/avatar")
    public Resp uploadAvatar(@RequestParam("file") MultipartFile file) {
        //检查参数
        if(file.isEmpty()) return Resp.error("头像不能为空");
        if(file.getSize() > 1024*1024*1) return Resp.error("头像不能超过1M");
        String fileName = UUID.randomUUID().toString();
        if(file.getOriginalFilename().toLowerCase().endsWith(".jpg")) fileName = fileName + ".jpg";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".jpeg")) fileName = fileName + ".jpeg";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".png")) fileName = fileName + ".png";
        else return Resp.error("上传头像仅支持jpg、png格式");
        //获取用户
        SysUser sysUser = sysUserRepository.findById(Oauth2Utils.getCurrentUser().getString("id")).orElse(null);
        if(sysUser == null) return Resp.error("异常！获取用户失败");
        //创建目录
        String path = "/avatar-res/";
        String folder = RES_PATH.replaceFirst("file:", "") + path;
        File targetFile = new File(folder);
        if (!targetFile.exists()) targetFile.mkdirs();
        //上传头像
        FileOutputStream out;
        try {
            out = new FileOutputStream(folder + fileName);
            out.write(file.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Resp.error("上传时获取文件失败");
        } catch (IOException e) {
            return Resp.error("上传失败");
        }
        //保存头像
        String fileUrl = "/user" + path + fileName;
        sysUser.setAvatar(fileUrl);
        sysUserRepository.save(sysUser);
        return Resp.success(fileUrl);
    }

    /**
     * 修改密码
     * @param post
     * @return
     */
    @RequestMapping(value = "/update/password")
    public Resp updatePassword(@RequestBody @Valid SysUser post) {
        if (StringUtils.isEmpty(post.getOldPassword())) return Resp.error("原密码不能为空");
        if (StringUtils.isEmpty(post.getNewPassword())) return Resp.error("新密码不能为空");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser sysUser = sysUserRepository.findByUsername(username);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(post.getOldPassword(), sysUser.getPassword())) return Resp.error("原密码无效");
        sysUser.setPassword(bCryptPasswordEncoder.encode(post.getNewPassword()));
        return Resp.success(sysUserRepository.save(sysUser));
    }

    /**
     * 绑定手机号
     * @param post
     * @return
     */
    @RequestMapping(value = "/update/phone")
    public Resp updatePhone(@RequestBody @Valid SysUser post) {
        if(StringUtils.isEmpty(post.getPhone())) return Resp.error("手机号不能为空");
        if(!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return Resp.error("手机号无效");
        if(StringUtils.isEmpty(post.getSmsCode())) return Resp.error("短信验证码不能为空");
        if(!smsService.verifySmsCode(post.getPhone(), post.getSmsCode())) return Resp.error("短信验证码无效");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser sysUser = sysUserRepository.findByUsername(username);
        SysUser existPhone = sysUserRepository.findByPhone(post.getPhone());
        if(post.getPhone().equals(sysUser.getPhone())) return Resp.error("手机号已绑定");
        if(!ObjectUtils.isEmpty(existPhone) && !existPhone.getId().equals(sysUser.getId())) return Resp.error("手机号已存在");
        sysUser.setPhone(post.getPhone());
        return Resp.success(sysUserRepository.save(sysUser));
    }

    /**
     * 超管创建客户经理
     * @param post
     * @return
     */
    @PostMapping("/createAdmin")
    public Resp createAdmin(@RequestBody SysUser post) {
        if(!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_SUPER.getCode())) return Resp.error("不是超级管理员，无权操作");
        if(StringUtils.isEmpty(post.getUsername())) return Resp.error("用户名不能为空");
        if(StringUtils.isEmpty(post.getRole())) return Resp.error("角色不能为空");
        if(StringUtils.isEmpty(post.getId())) {
            if(StringUtils.isEmpty(post.getNewPassword())) return Resp.error("密码不能为空");
            SysUser existUserName = sysUserRepository.findByUsername(post.getUsername());
            if(existUserName != null) return Resp.error("用户名已存在");
            post.setId(SqlUtils.createId());
            post.setPassword(new BCryptPasswordEncoder().encode(post.getNewPassword()));
            if(!StringUtils.isEmpty(post.getPhone())) {
                if(!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return Resp.error("手机号无效");
                SysUser existPhone = sysUserRepository.findByIdCardNo(post.getPhone());
                if(!ObjectUtils.isEmpty(existPhone)) return Resp.error("手机号已存在");
            }
            post.setCreateUser(Oauth2Utils.getCurrentUser().getString("id"));
            post.setCreateTime(SqlUtils.getDateTime());
        } else {
            SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
            if(ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
            if(!StringUtils.isEmpty(post.getNewPassword())) {
                post.setPassword(new BCryptPasswordEncoder().encode(post.getNewPassword()));
            } else post.setPassword(sysUser.getPassword());
            if(!StringUtils.isEmpty(post.getPhone())) {
                if(!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return Resp.error("手机号无效");
                SysUser existPhone = sysUserRepository.findByIdCardNo(post.getPhone());
                if(!ObjectUtils.isEmpty(existPhone) && !existPhone.getId().equals(sysUser.getId())) return Resp.error("手机号已存在");
            }
            post.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
            post.setUpdateTime(SqlUtils.getDateTime());
        }
        post.setEnterpriseId(Oauth2Utils.getCurrentUser().getString("enterpriseId"));
        post.setAccountNonExpired(true);
        post.setAccountNonLocked(true);
        post.setCredentialsNonExpired(true);
        post.setEnabled(true);
        return Resp.success(sysUserRepository.save(post));
    }

    /**
     * 修改管理员密码
     * @param post
     * @return
     */
    @RequestMapping(value = "/updatePassword")
    public Resp updateAdminPassword(@RequestBody SysUser post) {
        if(!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_SUPER.getCode())) return Resp.error("不是超级管理员，无权操作");
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        if(StringUtils.isEmpty(post.getNewPassword())) return Resp.error("新密码不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if (ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        sysUser.setPassword(bCryptPasswordEncoder.encode(post.getNewPassword()));
        sysUser.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
        sysUser.setUpdateTime(SqlUtils.getDateTime());
        return Resp.success(sysUserRepository.save(sysUser));
    }

    /**
     * 启用禁用用户
     * @param post
     * @return
     */
    @PostMapping("/update/enabled")
    public Resp updateEnabled(@RequestBody SysUser post) {
        if(!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_SUPER.getCode())) return Resp.error("不是超级管理员，无权操作");
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        if(StringUtils.isEmpty(post.getEnabled())) return Resp.error("启用禁用不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
        sysUser.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
        sysUser.setUpdateTime(SqlUtils.getDateTime());
        sysUser.setEnabled(post.getEnabled());
        return Resp.success(sysUserRepository.save(sysUser));
    }

    /**
     * 设置权限组
     * @param post
     * @return
     */
    @PostMapping("/update/group")
    public Resp updateGroup(@RequestBody SysUser post) {
        if(!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_SUPER.getCode())) return Resp.error("不是超级管理员，无权操作");
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        if(StringUtils.isEmpty(post.getGroupId())) return Resp.error("组id不能为空");
        SysUser sysUser = sysUserRepository.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysUser)) return Resp.error("id无效");
        sysUser.setGroupId(post.getGroupId());
        sysUser.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
        sysUser.setUpdateTime(SqlUtils.getDateTime());
        return Resp.success(sysUserRepository.save(sysUser));
    }
}
