package com.dusizhong.examples.cloud.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dusizhong.examples.cloud.user.entity.SysUser;
import com.dusizhong.examples.cloud.user.enums.RoleEnum;
import com.dusizhong.examples.cloud.user.model.BaseResp;
import com.dusizhong.examples.cloud.user.model.PageReq;
import com.dusizhong.examples.cloud.user.model.PageResp;
import com.dusizhong.examples.cloud.user.repository.SysUserRepository;
import com.dusizhong.examples.cloud.user.util.Oauth2Utils;
import com.dusizhong.examples.cloud.user.util.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Value("${resources.static-locations}")
    private String STATIC_PATH;
    @Value("${resources.static-url}")
    private String STATIC_URL;

    @Autowired
    private SysUserRepository sysUserRepository;

    @PostMapping("/create")
    public BaseResp create(@RequestBody SysUser post) {
        if(!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_ADMIN.getCode())) return BaseResp.error("无权操作");
        if(StringUtils.isEmpty(post.getUsername())) return BaseResp.error("用户名不能为空");
        if(StringUtils.isEmpty(post.getNewPassword())) return BaseResp.error("密码不能为空");
        post.setId(SqlUtils.createId());
        post.setPassword(new BCryptPasswordEncoder().encode(post.getNewPassword()));
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
        post.setCreateUser(Oauth2Utils.getCurrentUser().getString("id"));
        post.setCreateTime(SqlUtils.getDateTime());
        return BaseResp.success(sysUserRepository.save(post));
    }

    @PostMapping("/update")
    public BaseResp update(@RequestBody SysUser post) {
        if (!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_ADMIN.getCode())) return BaseResp.error("无权操作");
        if (StringUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findOne(post.getId());
        if (ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        if (!StringUtils.isEmpty(post.getNewPassword())) sysUser.setPassword(new BCryptPasswordEncoder().encode(post.getNewPassword()));
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
        sysUser.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
        sysUser.setUpdateTime(SqlUtils.getDateTime());
        return BaseResp.success(sysUserRepository.save(sysUser));
    }

    @PostMapping("/list")
    public BaseResp list(@RequestBody SysUser post) {
        Page<SysUser> pageData = sysUserRepository.findAll(new Specification<SysUser>() {
            @Override
            public Predicate toPredicate(Root<SysUser> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if(!StringUtils.isEmpty(post.getUsername())) {
                    predicates.add(cb.equal(root.get("username"), post.getUsername()));
                }
                if(!StringUtils.isEmpty(post.getRole())) {
                    predicates.add(cb.like(root.get("role"), "%" + post.getRole() + "%"));
                }
                if(!StringUtils.isEmpty(post.getPhone())) {
                    predicates.add(cb.equal(root.get("phone"), post.getPhone()));
                }
                if(!StringUtils.isEmpty(post.getEnterpriseId())) {
                    predicates.add(root.get("enterpriseId").in(post.getEnterpriseId().split(",")));
                }
                if(!StringUtils.isEmpty(post.getEnabled())) {
                    predicates.add(cb.equal(root.get("enabled"), post.getEnabled()));
                }
                return cq.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
            }
        }, PageReq.of(post.getPageNum(), post.getPageSize()));
        return BaseResp.success(PageResp.of(pageData));
    }

    @PostMapping("/detail")
    public BaseResp detail(@RequestBody SysUser post) {
        if(StringUtils.isEmpty(post.getId())) post.setId(Oauth2Utils.getCurrentUser().getString("id"));
        SysUser sysUser = sysUserRepository.findOne(post.getId());
        if(ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        if(!StringUtils.isEmpty(sysUser.getAvatar())) sysUser.setAvatar(STATIC_URL + sysUser.getAvatar());
        return BaseResp.success(sysUser);
    }

    @PostMapping("/delete")
    public BaseResp delete(@RequestBody SysUser post) {
        if(!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_ADMIN.getCode())) return BaseResp.error("无权操作");
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        SysUser sysUser = sysUserRepository.findOne(post.getId());
        if(ObjectUtils.isEmpty(sysUser)) return BaseResp.error("id无效");
        sysUserRepository.delete(post.getId());
        log.info("DELETE: {}", JSONObject.toJSONString(sysUser, SerializerFeature.WriteMapNullValue));
        return BaseResp.success();
    }

    @PostMapping("/avatar")
    public BaseResp uploadAvatar(@RequestParam("file") MultipartFile file) {
        if(file.isEmpty()) return BaseResp.error("头像不能为空");
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();
        if(!suffix.equals(".jpg") && !suffix.equals("png")) return BaseResp.error("头像仅支持jpg、png格式");
        if(file.getSize() > 1024*1024*1) return BaseResp.error("头像不能超过1M");
        SysUser sysUser = sysUserRepository.findOne(Oauth2Utils.getCurrentUser().getString("id"));
        if(ObjectUtils.isEmpty(sysUser)) return BaseResp.error("异常！获取用户失败");
        String fileName = UUID.randomUUID() + suffix;

        String path = "/avatar-res/";
        String filePath = STATIC_PATH.replaceFirst("file:", "") + path;
        try {
            Files.write(Paths.get(filePath + fileName), file.getBytes());
        } catch (IOException e) {
            return BaseResp.error("头像上传失败" + e.getMessage());
        }

//        File targetFile = new File(folder);
//        if (!targetFile.exists()) targetFile.mkdirs();
//        FileOutputStream out;
//        try {
//            out = new FileOutputStream(folder + fileName);
//            out.write(file.getBytes());
//            out.flush();
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return BaseResp.error("上传时获取文件失败");
//        } catch (IOException e) {
//            return BaseResp.error("上传失败");
//        }
        //保存头像
        String fileUrl = path + fileName;
        sysUser.setAvatar(fileName);
        sysUser.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
        sysUser.setUpdateTime(SqlUtils.getDateTime());
        sysUserRepository.save(sysUser);
        return BaseResp.success(STATIC_URL + sysUser.getAvatar());
    }

    @RequestMapping(value = "/modifyPassword")
    public BaseResp modifyPassword(@RequestBody @Valid SysUser post) {
        if (StringUtils.isEmpty(post.getOldPassword())) return BaseResp.error("原密码不能为空");
        if (StringUtils.isEmpty(post.getNewPassword())) return BaseResp.error("新密码不能为空");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser sysUser = sysUserRepository.findByUsername(username);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(post.getOldPassword(), sysUser.getPassword())) return BaseResp.error("原密码无效");
        sysUser.setPassword(bCryptPasswordEncoder.encode(post.getNewPassword()));
        sysUser.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
        sysUser.setUpdateTime(SqlUtils.getDateTime());
        return BaseResp.success(sysUserRepository.save(sysUser));
    }

    @RequestMapping(value = "/modifyPhone")
    public BaseResp modifyPhone(@RequestBody @Valid SysUser post) {
        if(StringUtils.isEmpty(post.getPhone())) return BaseResp.error("手机号不能为空");
        if(!post.getPhone().startsWith("1") || post.getPhone().length() != 11) return BaseResp.error("手机号无效");
        if(StringUtils.isEmpty(post.getSmsCode())) return BaseResp.error("短信验证码不能为空");
        //if(!smsService.verifySmsCode(post.getPhone(), post.getSmsCode())) return BaseResp.error("短信验证码无效");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser sysUser = sysUserRepository.findByUsername(username);
        SysUser existPhone = sysUserRepository.findByPhone(post.getPhone());
        if(post.getPhone().equals(sysUser.getPhone())) return BaseResp.error("手机号已绑定");
        if(!ObjectUtils.isEmpty(existPhone) && !existPhone.getId().equals(sysUser.getId())) return BaseResp.error("手机号已存在");
        sysUser.setPhone(post.getPhone());
        sysUser.setUpdateUser(Oauth2Utils.getCurrentUser().getString("id"));
        sysUser.setUpdateTime(SqlUtils.getDateTime());
        return BaseResp.success(sysUserRepository.save(sysUser));
    }
}
