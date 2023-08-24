package com.dusizhong.examples.jpa.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dusizhong.examples.jpa.dao.SysEnterpriseRepository;
import com.dusizhong.examples.jpa.dao.SysUserRepository;
import com.dusizhong.examples.jpa.entity.SysEnterprise;
import com.dusizhong.examples.jpa.enums.StatusEnum;
import com.dusizhong.examples.jpa.model.BaseResp;
import com.dusizhong.examples.jpa.model.PageReq;
import com.dusizhong.examples.jpa.model.PageResp;
import com.dusizhong.examples.jpa.util.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
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
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/enterprise")
public class SysEnterpriseController {

    @Value("${spring.resources.static-locations}")
    private String RES_PATH;
    @Value("${resources.static-url}")
    private String RES_URL;

    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;
    @Autowired
    private SysUserRepository sysUserRepository;

    @Transactional
    @PostMapping("/save")
    public BaseResp save(@RequestBody @Valid SysEnterprise post) {
        if(StringUtils.isEmpty(post.getEnterpriseName())) return BaseResp.error("单位名称不能为空");
        SysEnterprise existEnterpriseName = sysEnterpriseRepository.findByEnterpriseName(post.getEnterpriseName());
        if(StringUtils.isEmpty(post.getId())) {
            if(!ObjectUtils.isEmpty(existEnterpriseName)) return BaseResp.error("单位名称已存在");
            if(!StringUtils.isEmpty(post.getEnterpriseCode())) {
                SysEnterprise existEnterpriseCode = sysEnterpriseRepository.findByEnterpriseCode(post.getEnterpriseCode());
                if(!ObjectUtils.isEmpty(existEnterpriseCode)) return BaseResp.error("统一社会信用代码已存在");
            }
            post.setId(SqlUtils.createId());
        } else {
            SysEnterprise sysEnterprise = sysEnterpriseRepository.findOne(post.getId());
            if(ObjectUtils.isEmpty(sysEnterprise)) return BaseResp.error("单位id无效");
            if(!ObjectUtils.isEmpty(existEnterpriseName) && !existEnterpriseName.getId().equals(sysEnterprise.getId())) return BaseResp.error("单位名称已存在");
            if(!StringUtils.isEmpty(post.getEnterpriseCode())) {
                SysEnterprise existEnterpriseCode = sysEnterpriseRepository.findByEnterpriseCode(post.getEnterpriseCode());
                if(!ObjectUtils.isEmpty(existEnterpriseCode) && !existEnterpriseCode.getId().equals(sysEnterprise.getId())) return BaseResp.error("统一社会信用代码已存在");
            }
        }
        post.setStatus(StatusEnum.EDIT.getCode());
        return BaseResp.success(sysEnterpriseRepository.save(post));
    }

    @PostMapping("/list")
    public BaseResp list(@RequestBody SysEnterprise post) {
        Page<SysEnterprise> pageData = sysEnterpriseRepository.findAll(new Specification<SysEnterprise>() {
            @Override
            public Predicate toPredicate(Root<SysEnterprise> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if(!StringUtils.isEmpty(post.getId())) {
                    predicates.add(root.get("id").in(post.getId().split(",")));
                }
                if(!StringUtils.isEmpty(post.getEnterpriseName())) {
                    predicates.add(cb.like(root.get("enterpriseName"), "%" + post.getEnterpriseName() + "%"));
                }
                if(!StringUtils.isEmpty(post.getEnterpriseCode())) {
                    predicates.add(cb.equal(root.get("enterpriseCode"), post.getEnterpriseCode()));
                }
                if(!StringUtils.isEmpty(post.getEnterpriseRole())) {
                    predicates.add(cb.like(root.get("enterpriseRole"), "%" + post.getEnterpriseRole() + "%"));
                }
                if(!StringUtils.isEmpty(post.getStatus())) {
                    predicates.add(cb.equal(root.get("status"), post.getStatus()));
                }
                return cq.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
            }
        }, PageReq.of(post.getPageNum(), post.getPageSize()));
        return BaseResp.success(PageResp.of(pageData));
    }

    @PostMapping("/list1")
    public BaseResp list1(@RequestBody SysEnterprise post) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("enterpriseName", ExampleMatcher.GenericPropertyMatchers.contains()) //模糊查询
                .withMatcher("enterpriseCode", ExampleMatcher.GenericPropertyMatchers.exact()) //精确查询
                .withMatcher("status", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.STARTING)) //精确查询，忽略""
                .withIgnorePaths("id"); //忽略按id查询
                //其他未定义字段默认为精确查询
        Example<SysEnterprise> example = Example.of(post, matcher);
        Page<SysEnterprise> pageData = sysEnterpriseRepository.findAll(example, PageReq.of(post.getPageNum(), post.getPageSize()));
        return BaseResp.success(PageResp.of(pageData));
    }

    @RequestMapping("list2")
    public BaseResp list2(@RequestBody SysEnterprise post) {
        Example<SysEnterprise> example = Example.of(post);
        List<SysEnterprise> enterpriseList = sysEnterpriseRepository.findAll(example);
        Pageable pageable = new PageRequest(post.getPageNum(), post.getPageSize(), Sort.Direction.DESC, "id");
        int start = pageable.getOffset() > enterpriseList.size()? enterpriseList.size() : pageable.getOffset();
        int end = (start + pageable.getPageSize()) > enterpriseList.size()? enterpriseList.size() : (start + pageable.getPageSize());
        Page<SysEnterprise> pageData =  new PageImpl<>(enterpriseList.subList(start, end), pageable, enterpriseList.size());
        return BaseResp.success(pageData);
    }

    @PostMapping("/detail")
    public BaseResp detail(@RequestBody @Valid SysEnterprise post) {
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("单位id不能为空");
        SysEnterprise sysEnterprise = sysEnterpriseRepository.findOne(post.getId());
        if(ObjectUtils.isEmpty(sysEnterprise)) return BaseResp.error("单位id无效");
        if(!StringUtils.isEmpty(sysEnterprise.getLicensePic())) sysEnterprise.setLicensePic(RES_URL + sysEnterprise.getLicensePic());
        if(!StringUtils.isEmpty(sysEnterprise.getLegalIdCardPic())) sysEnterprise.setLegalIdCardPic(RES_URL + sysEnterprise.getLegalIdCardPic());
        if(!StringUtils.isEmpty(sysEnterprise.getRegisterIdCardPic())) sysEnterprise.setRegisterIdCardPic(RES_URL + sysEnterprise.getRegisterIdCardPic());
        if(!StringUtils.isEmpty(sysEnterprise.getLegalAuthorizePic())) sysEnterprise.setLegalAuthorizePic(RES_URL + sysEnterprise.getLegalAuthorizePic());
        return BaseResp.success(sysEnterprise);
    }

    @PostMapping("/delete")
    public BaseResp delete(@RequestBody @Valid SysEnterprise post) {
        //if(!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_ADMIN.getCode())) return BaseResp.error("无权操作");
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("单位id不能为空");
        SysEnterprise sysEnterprise = sysEnterpriseRepository.findOne(post.getId());
        if(sysEnterprise == null) return BaseResp.error("单位id无效");
        sysEnterpriseRepository.delete(sysEnterprise);
        log.info("DELETE: {}", JSONObject.toJSONString(sysEnterprise, SerializerFeature.WriteMapNullValue));
        return BaseResp.success();
    }
}
