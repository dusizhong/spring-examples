package com.dusizhong.examples.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.entity.SysUserInfoMaterial;
import com.dusizhong.examples.user.enums.StatusEnum;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.entity.SysUserInfo;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.SysUserInfoRepository;
import com.dusizhong.examples.user.repository.SysUserInfoMaterialRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.PageHelper;
import com.dusizhong.examples.user.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.List;
import java.util.Optional;

/**
 * 用户信息接口
 * @author Dusizhong
 * @since 2022-09-26
 */
@RestController
@RequestMapping("/user/info")
public class SysUserInfoController {

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserInfoRepository sysUserInfoRepository;
    @Autowired
    private SysUserInfoMaterialRepository sysUserInfoMaterialRepository;

    /**
     * 保存个人信息
     * @param post
     * @return
     */
    @PostMapping("/save")
    public Resp save(@RequestBody SysUserInfo post) {
        if(StringUtils.isEmpty(post.getId())) {
            SysUserInfo sysUserInfo = sysUserInfoRepository.findByUserId(Oauth2Utils.getCurrentUser().getString("id"));
            if (!ObjectUtils.isEmpty(sysUserInfo)) return Resp.error("用户信息已存在，请传id更新");
            if (!StringUtils.isEmpty(post.getIdCardNo())) {
                if (post.getIdCardNo().length() != 18) return Resp.error("身份证号应为18位");
                SysUserInfo existIdCardNo = sysUserInfoRepository.findByIdCardNo(post.getIdCardNo());
                if (!ObjectUtils.isEmpty(existIdCardNo)) return Resp.error("身份证号已存在");
            }
            post.setUserId(Oauth2Utils.getCurrentUser().getString("id"));
            post.setStatus(StatusEnum.EDIT.name());
            sysUserInfo = sysUserInfoRepository.save(post);
            //同步更新用户
            SysUser sysUser = sysUserRepository.findById(sysUserInfo.getUserId()).orElse(null);
            if (sysUser == null) return Resp.error("异常！获取用户失败");
            sysUser.setUserInfoId(sysUserInfo.getId());
            sysUserRepository.save(sysUser);
            return Resp.success(sysUserInfo);
        } else {
            SysUserInfo sysUserInfo = sysUserInfoRepository.findById(post.getId()).orElse(null);
            if(sysUserInfo == null) return Resp.error("id无效");
            //if(StatusEnum.APPROVAL.getCode().equals(sysUserInfo.getStatus())) return Resp.error("审核通过状态，不能操作");
            if(!Oauth2Utils.getCurrentUser().getString("id").equals(sysUserInfo.getCreateUser())) return Resp.error("创建人不符，无权操作");
            if(!StringUtils.isEmpty(post.getIdCardNo())) {
                if (post.getIdCardNo().length() != 18) return Resp.error("身份证号应为18位");
                SysUserInfo existIdCardNo = sysUserInfoRepository.findByIdCardNo(post.getIdCardNo());
                if (!ObjectUtils.isEmpty(existIdCardNo) && !existIdCardNo.getId().equals(sysUserInfo.getId())) return Resp.error("身份证号已存在");
            }
            post.setUserId(sysUserInfo.getUserId());
            post.setStatus(StatusEnum.EDIT.name());
            return Resp.success(sysUserInfoRepository.save(post));
        }
    }

    @PostMapping("/list")
    public Resp list(@RequestBody SysUserInfo post) {
        Page<SysUserInfo> page = sysUserInfoRepository.findAll(new Specification<SysUserInfo>() {
            @Override
            public Predicate toPredicate(Root<SysUserInfo> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if(!StringUtils.isEmpty(post.getName())) {
                    predicates.add(criteriaBuilder.equal(root.get("name"), post.getName()));
                }
                if(!StringUtils.isEmpty(post.getIdCardNo())) {
                    predicates.add(criteriaBuilder.equal(root.get("idCardNo"), post.getIdCardNo()));
                }
                if(!StringUtils.isEmpty(post.getEvalMajor())) {
                    predicates.add(criteriaBuilder.like(root.get("evalMajor"), "%" + post.getEvalMajor() + "%"));
                }
                if(!StringUtils.isEmpty(post.getEnterpriseName())) {
                    predicates.add(criteriaBuilder.equal(root.get("enterpriseName"), post.getEnterpriseName()));
                }
                if(!StringUtils.isEmpty(post.getStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), post.getStatus()));
                }
                return criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
            }
        }, PageHelper.of(post.getPageNumber(), post.getPageSize()));
        return Resp.success(page);
    }

    /**
     * 用户信息详情
     * @param post
     * @return
     */
    @PostMapping("/detail")
    public Resp detail(@RequestBody SysUserInfo post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        SysUserInfo sysUserInfo = sysUserInfoRepository.findById(post.getId()).orElse(null);
        if(sysUserInfo == null) return Resp.error("id无效");
        List<SysUserInfoMaterial> materialList = sysUserInfoMaterialRepository.findByUserId(sysUserInfo.getUserId());
        sysUserInfo.setMaterialList(materialList);
        return Resp.success(sysUserInfo);
    }




    @Transactional
    @PostMapping("/createUserAndInfo")
    public Resp createUserAndInfo(@RequestBody SysUserInfo post) {
        if(StringUtils.isEmpty(post.getName())) return Resp.error("姓名不能为空");
        if(StringUtils.isEmpty(post.getRole())) return Resp.error("角色不能为空");
        if(!post.getRole().equals("ROLE_EXPERT") && !post.getRole().equals("ROLE_SUPERVISOR")) return Resp.error("角色值无效");
        if(StringUtils.isEmpty(post.getIdCardNo())) return Resp.error("身份证号不能为空");
        if (post.getIdCardNo().length() != 18) return Resp.error("身份证号应为18位");
        SysUserInfo existIdCardNo = sysUserInfoRepository.findByIdCardNo(post.getIdCardNo());
        if (!ObjectUtils.isEmpty(existIdCardNo)) return Resp.error("身份证号已存在");
        //创建用户
        SysUser sysUser = new SysUser();
        sysUser.setId(SqlUtils.createId());
        sysUser.setUsername(post.getIdCardNo());
        sysUser.setPassword(new BCryptPasswordEncoder().encode(post.getIdCardNo().substring(post.getIdCardNo().length() - 6)));
        sysUser.setRole(post.getRole());
        sysUser.setCreateTime(SqlUtils.getDateTime());
        sysUserRepository.save(sysUser);
        //创建用户信息
        post.setId(SqlUtils.createId());
        post.setUserId(sysUser.getId());
        post.setStatus(StatusEnum.NEW.name());
        JSONObject user = Oauth2Utils.getCurrentUser();
        post.setCreateUser(user.getString("id"));
        post.setCreateTime(SqlUtils.getDateTime());
        return Resp.success(sysUserInfoRepository.save(post));
    }

    @Transactional
    @PostMapping("/updateUserAndInfo")
    public Resp updateUserAndInfo(@RequestBody SysUserInfo post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        if(StringUtils.isEmpty(post.getName())) return Resp.error("姓名不能为空");
        if(StringUtils.isEmpty(post.getRole())) return Resp.error("角色不能为空");
        if(!post.getRole().equals("ROLE_EXPERT") && !post.getRole().equals("ROLE_SUPERVISOR")) return Resp.error("角色值无效");
        if(StringUtils.isEmpty(post.getIdCardNo())) return Resp.error("身份证号不能为空");
        if (post.getIdCardNo().length() != 18) return Resp.error("身份证号应为18位");
        Optional<SysUserInfo> sysUserInfo = sysUserInfoRepository.findById(post.getId());
        if(!sysUserInfo.isPresent()) return Resp.error("id无效");
        SysUserInfo existIdCardNo = sysUserInfoRepository.findByIdCardNo(post.getIdCardNo());
        if (!ObjectUtils.isEmpty(existIdCardNo) && !existIdCardNo.getId().equals(sysUserInfo.get().getId())) return Resp.error("身份证号已存在");
        Optional<SysUser> sysUser = sysUserRepository.findById(sysUserInfo.get().getUserId());
        if(!sysUser.isPresent()) return Resp.error("个人信息中用户id无效");
        //更新用户
        sysUser.get().setUsername(post.getIdCardNo());
        sysUser.get().setPassword(post.getIdCardNo().substring(post.getIdCardNo().length() - 6));
        sysUser.get().setRole(post.getRole());
        sysUser.get().setUpdateTime(SqlUtils.getDateTime());
        sysUserRepository.save(sysUser.get());
        //更新用户信息
        post.setUserId(sysUser.get().getId());
        JSONObject user = Oauth2Utils.getCurrentUser();
        post.setCreateUser(user.getString("id"));
        post.setCreateTime(SqlUtils.getDateTime());
        return Resp.success(sysUserInfoRepository.save(post));
    }

    @PostMapping("/deleteUserAndInfo")
    public Resp deleteUserAndInfo(@RequestBody SysUserInfo post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        Optional<SysUserInfo> sysUserInfo = sysUserInfoRepository.findById(post.getId());
        if(!sysUserInfo.isPresent()) return Resp.error("id无效");
        if("NEW".equals(sysUserInfo.get().getStatus())) return Resp.error("状态不符，不能删除");
        JSONObject user = Oauth2Utils.getCurrentUser();
        if(!user.getString("id").equals(sysUserInfo.get().getCreateUser())) return Resp.error("创建人不符，不能删除");
        sysUserRepository.deleteById(sysUserInfo.get().getUserId());
        sysUserInfoRepository.deleteById(sysUserInfo.get().getId());
        return Resp.success();
    }
}
