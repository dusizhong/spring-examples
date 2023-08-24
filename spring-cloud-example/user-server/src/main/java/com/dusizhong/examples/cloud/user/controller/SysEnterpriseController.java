package com.dusizhong.examples.cloud.user.controller;

import com.dusizhong.examples.cloud.user.entity.SysEnterprise;
import com.dusizhong.examples.cloud.user.entity.SysUser;
import com.dusizhong.examples.cloud.user.enums.StatusEnum;
import com.dusizhong.examples.cloud.user.model.BaseResp;
import com.dusizhong.examples.cloud.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.cloud.user.repository.SysUserRepository;
import com.dusizhong.examples.cloud.user.util.Oauth2Utils;
import com.dusizhong.examples.cloud.user.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enterprise")
public class SysEnterpriseController {

    @Value("${resources.static-locations}")
    private String RES_PATH;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;
    @Autowired
    private SysUserRepository sysUserRepository;

    @Transactional
    @PostMapping("/save")
    public BaseResp save(@RequestBody @Valid SysEnterprise post) {
        if(StringUtils.isEmpty(post.getEnterpriseName())) return BaseResp.error("单位名称不能为空");
        if(StringUtils.isEmpty(post.getEnterpriseCode())) return BaseResp.error("统一信用代码不能为空");
        if(StringUtils.isEmpty(post.getStatus())) return BaseResp.error("状态不能为空");
        if(!StatusEnum.EDIT.getCode().equals(post.getStatus()) && !StatusEnum.SUBMIT.getCode().equals(post.getStatus())) return BaseResp.error("状态值无效");
        SysUser sysUser = sysUserRepository.findOne(Oauth2Utils.getCurrentUser().getString("id"));
        if(sysUser == null) return BaseResp.error("异常！获取当前用户失败");
        SysEnterprise existEnterpriseName = sysEnterpriseRepository.findByEnterpriseName(post.getEnterpriseName());
        SysEnterprise existEnterpriseCode = sysEnterpriseRepository.findByEnterpriseCode(post.getEnterpriseCode());
        if(StringUtils.isEmpty(post.getId())) {
            if(!ObjectUtils.isEmpty(existEnterpriseName)) return BaseResp.error("单位名称已存在");
            if(!ObjectUtils.isEmpty(existEnterpriseCode)) return BaseResp.error("统一信用代码已存在");
            post.setId(SqlUtils.createId());
        } else {
            SysEnterprise sysEnterprise = sysEnterpriseRepository.findOne(post.getId());
            if(ObjectUtils.isEmpty(sysEnterprise)) return BaseResp.error("单位id无效");
            if(!Oauth2Utils.getCurrentUser().getString("id").equals(sysEnterprise.getCreateUser())) return BaseResp.error("创建人不符，无权操作");
            if(!ObjectUtils.isEmpty(existEnterpriseName) && !existEnterpriseName.getId().equals(sysEnterprise.getId())) return BaseResp.error("单位名称已存在");
            if(!ObjectUtils.isEmpty(existEnterpriseCode) && !existEnterpriseCode.getId().equals(sysEnterprise.getId())) return BaseResp.error("统一信用代码已存在");
            //0324新增：将用户角色置成单位角色
            post.setEnterpriseRole(sysUser.getRole());
            post.setLicensePic(sysEnterprise.getLicensePic());
            post.setLegalIdCardPic(sysEnterprise.getLegalIdCardPic());
            post.setRegisterIdCardPic(sysEnterprise.getRegisterIdCardPic());
            post.setLegalAuthorizePic(sysEnterprise.getLegalAuthorizePic());
//            if(StatusEnum.SUBMIT.getCode().equals(post.getStatus())) {
//                //生成审核记录
//                SysApprovalRecord sysApprovalRecord = new SysApprovalRecord();
//                sysApprovalRecord.setId(SqlUtils.createId());
//                sysApprovalRecord.setApprovalItemType("ENTERPRISE");
//                sysApprovalRecord.setApprovalItemId(sysEnterprise.getId());
//                sysApprovalRecord.setApprovalItemName(sysEnterprise.getEnterpriseName());
//                sysApprovalRecord.setSubmitUserId(Oauth2Utils.getCurrentUser().getString("id"));
//                sysApprovalRecord.setSubmitUserName(Oauth2Utils.getCurrentUser().getString("username"));
//                sysApprovalRecord.setSubmitRemark(post.getRemark());
//                sysApprovalRecord.setSubmitTime(SqlUtils.getDateTime());
//                sysApprovalRecord.setStatus(StatusEnum.SUBMIT.getCode());
//                sysApprovalRecord = sysApprovalRecordRepository.save(sysApprovalRecord);
//                post.setApprovalRecordId(sysApprovalRecord.getId());
//            }
        }
        SysEnterprise sysEnterprise = sysEnterpriseRepository.save(post);
        //同步更新用户
        sysUser.setEnterpriseId(sysEnterprise.getId());
        sysUserRepository.save(sysUser);
        return BaseResp.success(sysEnterprise);
    }

    @PostMapping("/list")
    public BaseResp list(@RequestBody @Valid SysEnterprise post) {
        List<SysEnterprise> sysEnterpriseList = sysEnterpriseRepository.findAll(new Sort(Sort.Direction.DESC, "createTime"));
        if(!StringUtils.isEmpty(post.getId())) {
            sysEnterpriseList = sysEnterpriseList.stream().filter(s -> post.getId().contains(s.getId())).collect(Collectors.toList());
        }
        if(!StringUtils.isEmpty(post.getEnterpriseName())) {
            sysEnterpriseList = sysEnterpriseList.stream().filter(s -> s.getEnterpriseName().contains(post.getEnterpriseName())).collect(Collectors.toList());
        }
        if(!StringUtils.isEmpty(post.getEnterpriseCode())) {
            sysEnterpriseList = sysEnterpriseList.stream().filter(s -> s.getEnterpriseCode().contains(post.getEnterpriseCode())).collect(Collectors.toList());
        }
        if(!StringUtils.isEmpty(post.getEnterpriseRole())) {
            sysEnterpriseList = sysEnterpriseList.stream().filter(s -> s.getEnterpriseRole().contains(post.getEnterpriseRole())).collect(Collectors.toList());
        }
        if(!StringUtils.isEmpty(post.getStatus())) {
            sysEnterpriseList = sysEnterpriseList.stream().filter(s -> post.getStatus().contains(s.getStatus())).collect(Collectors.toList());
        }
        Pageable pageable = new PageRequest(post.getPageNum(), post.getPageSize());
        long start = pageable.getOffset() > sysEnterpriseList.size() ? sysEnterpriseList.size() : pageable.getOffset();
        long end = (start + pageable.getPageSize()) > sysEnterpriseList.size() ? sysEnterpriseList.size() : (start + pageable.getPageSize());
        Page<SysEnterprise> pageData = new PageImpl<>(sysEnterpriseList.subList((int) start, (int) end), pageable, sysEnterpriseList.size());
        return BaseResp.success(pageData);
    }

//    @PostMapping("/list")
//    public Resp list(@RequestBody @Valid SysEnterprise query) {
//        Page<SysEnterprise> page = sysEnterpriseRepository.findAll(new Specification<SysEnterprise>() {
//            @Override
//            public Predicate toPredicate(Root<SysEnterprise> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
//                List<Predicate> predicates = new ArrayList<Predicate>();
//                if(!StringUtils.isEmpty(query.getId())) {
//                    predicates.add(root.get("id").in(query.getId().split(",")));
//                }
//                if(!StringUtils.isEmpty(query.getEnterpriseName())) {
//                    predicates.add(criteriaBuilder.like(root.get("enterpriseName"), "%" + query.getEnterpriseName() + "%"));
//                }
//                if(!StringUtils.isEmpty(query.getEnterpriseCode())) {
//                    predicates.add(criteriaBuilder.equal(root.get("enterpriseCode"), query.getEnterpriseCode()));
//                }
//                if(!StringUtils.isEmpty(query.getStatus())) {
//                    predicates.add(criteriaBuilder.equal(root.get("status"), query.getStatus()));
//                }
//                return criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
//            }
//        }, PageHelper.of(query.getPageNumber(), query.getPageSize()));
//        return Resp.success(page);
//    }

    @PostMapping("/detail")
    public BaseResp detail(@RequestBody @Valid SysEnterprise post) {
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("单位id不能为空");
        SysEnterprise sysEnterprise = sysEnterpriseRepository.findOne(post.getId());
        if(sysEnterprise == null) return BaseResp.error("单位id无效");
        return BaseResp.success(sysEnterprise);
    }

//    @PostMapping("/approval")
//    public Resp approval(@RequestBody SysEnterprise post) {
//        if(StringUtils.isEmpty(post.getId())) return Resp.error("单位id不能为空");
//        if(StringUtils.isEmpty(post.getStatus())) return Resp.error("状态不能为空");
//        SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(post.getId()).orElse(null);
//        if(sysEnterprise == null) return Resp.error("单位id无效");
//        if(!StatusEnum.SUBMIT.getCode().equals(sysEnterprise.getStatus())) return Resp.error("非待审核状态，不能操作");
//        //更新审核记录
//        SysApprovalRecord sysApprovalRecord = sysApprovalRecordRepository.findById(sysEnterprise.getApprovalRecordId()).orElse(null);
//        if(ObjectUtils.isEmpty(sysApprovalRecord)) return Resp.error("审核记录id无效");
//        sysApprovalRecord.setApprovalUserId(Oauth2Utils.getCurrentUser().getString("id"));
//        sysApprovalRecord.setApprovalUserName(Oauth2Utils.getCurrentUser().getString("username"));
//        sysApprovalRecord.setApprovalResult(post.getRemark());
//        sysApprovalRecord.setApprovalTime(SqlUtils.getDateTime());
//        sysApprovalRecord.setStatus(post.getStatus());
//        sysApprovalRecord = sysApprovalRecordRepository.save(sysApprovalRecord);
//        //更新单位状态
//        sysEnterprise.setStatus(post.getStatus());
//        sysEnterprise.setRemark(post.getRemark());
//        return Resp.success(sysEnterpriseRepository.save(sysEnterprise));
//    }
}