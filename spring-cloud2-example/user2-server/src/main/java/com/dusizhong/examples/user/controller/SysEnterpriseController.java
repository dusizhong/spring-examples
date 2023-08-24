package com.dusizhong.examples.user.controller;

import com.dusizhong.examples.user.entity.SysApprovalRecord;
import com.dusizhong.examples.user.entity.SysEnterprise;
import com.dusizhong.examples.user.entity.SysUser;
import com.dusizhong.examples.user.enums.RoleEnum;
import com.dusizhong.examples.user.enums.StatusEnum;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.SysApprovalRecordRepository;
import com.dusizhong.examples.user.repository.SysEnterpriseMaterialRepository;
import com.dusizhong.examples.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
import com.dusizhong.examples.user.util.Oauth2Utils;
import com.dusizhong.examples.user.util.SqlUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 单位接口
 * @author Dusizhong
 * @since 2022-09-22
 */
@RestController
@RequestMapping("/enterprise")
public class SysEnterpriseController {

    @Value("${spring.resources.static-locations}")
    private String RES_PATH;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;
    @Autowired
    private SysEnterpriseMaterialRepository sysEnterpriseMaterialRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysApprovalRecordRepository sysApprovalRecordRepository;

    /**
     * 新增或更新单位、提交审核
     * @param post
     * @return
     */
    @Transactional
    @PostMapping("/save")
    public Resp save(@RequestBody @Valid SysEnterprise post) {
        if(StringUtils.isEmpty(post.getEnterpriseName())) return Resp.error("单位名称不能为空");
        if(StringUtils.isEmpty(post.getEnterpriseCode())) return Resp.error("统一信用代码不能为空");
        if(StringUtils.isEmpty(post.getStatus())) return Resp.error("状态不能为空");
        if(!StatusEnum.EDIT.getCode().equals(post.getStatus()) && !StatusEnum.SUBMIT.getCode().equals(post.getStatus())) return Resp.error("状态值无效");
        SysUser sysUser = sysUserRepository.findById(Oauth2Utils.getCurrentUser().getString("id")).orElse(null);
        if (sysUser == null) return Resp.error("异常！获取当前用户失败");
        if(!sysUser.getRole().contains(RoleEnum.ROLE_AGENCY.getCode())
                && !sysUser.getRole().contains(RoleEnum.ROLE_BIDDER.getCode())) return Resp.error("不是投标人或代理角色，无须提交单位信息");
        SysEnterprise existEnterpriseName = sysEnterpriseRepository.findByEnterpriseName(post.getEnterpriseName());
        SysEnterprise existEnterpriseCode = sysEnterpriseRepository.findByEnterpriseCode(post.getEnterpriseCode());
        if(StringUtils.isEmpty(post.getId())) {
            if(!ObjectUtils.isEmpty(existEnterpriseName)) return Resp.error("单位名称已存在");
            if(!ObjectUtils.isEmpty(existEnterpriseCode)) return Resp.error("统一信用代码已存在");
            post.setId(SqlUtils.createId());
            post.setSid(System.currentTimeMillis() + RandomStringUtils.random(7, false, true));
        } else {
            SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(post.getId()).orElse(null);
            if(ObjectUtils.isEmpty(sysEnterprise)) return Resp.error("单位id无效");
            //与前端约定：去掉限制随时都可以修改
            //if(StatusEnum.SUBMIT.getCode().equals(sysEnterprise.getStatus())) return Resp.error("待审核状态，不能操作");
            if(!Oauth2Utils.getCurrentUser().getString("id").equals(sysEnterprise.getCreateUser())) return Resp.error("创建人不符，无权操作");
            if(!ObjectUtils.isEmpty(existEnterpriseName) && !existEnterpriseName.getId().equals(sysEnterprise.getId())) return Resp.error("单位名称已存在");
            if(!ObjectUtils.isEmpty(existEnterpriseCode) && !existEnterpriseCode.getId().equals(sysEnterprise.getId())) return Resp.error("统一信用代码已存在");
            post.setSid(sysEnterprise.getSid());
            //0324新增：将用户角色置成单位角色
            post.setEnterpriseRole(sysUser.getRole());
            post.setEnterpriseLicensePic(sysEnterprise.getEnterpriseLicensePic());
            post.setLegalIdCardPic(sysEnterprise.getLegalIdCardPic());
            post.setRegisterIdCardPic(sysEnterprise.getRegisterIdCardPic());
            post.setLegalAuthorizePic(sysEnterprise.getLegalAuthorizePic());
            if(StatusEnum.SUBMIT.getCode().equals(post.getStatus())) {
                //生成审核记录
                SysApprovalRecord sysApprovalRecord = new SysApprovalRecord();
                sysApprovalRecord.setId(SqlUtils.createId());
                sysApprovalRecord.setApprovalItemType("ENTERPRISE");
                sysApprovalRecord.setApprovalItemId(sysEnterprise.getId());
                sysApprovalRecord.setApprovalItemName(sysEnterprise.getEnterpriseName());
                sysApprovalRecord.setSubmitUserId(Oauth2Utils.getCurrentUser().getString("id"));
                sysApprovalRecord.setSubmitUserName(Oauth2Utils.getCurrentUser().getString("username"));
                sysApprovalRecord.setSubmitRemark(post.getRemark());
                sysApprovalRecord.setSubmitTime(SqlUtils.getDateTime());
                sysApprovalRecord.setStatus(StatusEnum.SUBMIT.getCode());
                sysApprovalRecord = sysApprovalRecordRepository.save(sysApprovalRecord);
                post.setApprovalRecordId(sysApprovalRecord.getId());
            }
        }
        SysEnterprise sysEnterprise = sysEnterpriseRepository.save(post);
        //同步更新用户
        sysUser.setEnterpriseId(sysEnterprise.getId());
        sysUserRepository.save(sysUser);
        return Resp.success(sysEnterprise);
    }

    @PostMapping("/list")
    public Resp list(@RequestBody @Valid SysEnterprise post) {
        List<SysEnterprise> sysEnterpriseList = sysEnterpriseRepository.findAll(Sort.by("createTime").descending());
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
        if (ObjectUtils.isEmpty(post.getPageNumber())) post.setPageNumber(0);
        if (ObjectUtils.isEmpty(post.getPageSize())) post.setPageSize(20);
        Pageable pageable = PageRequest.of(post.getPageNumber(), post.getPageSize());
        long start = pageable.getOffset() > sysEnterpriseList.size() ? sysEnterpriseList.size() : pageable.getOffset();
        long end = (start + pageable.getPageSize()) > sysEnterpriseList.size() ? sysEnterpriseList.size() : (start + pageable.getPageSize());
        Page<SysEnterprise> pageData = new PageImpl<>(sysEnterpriseList.subList((int) start, (int) end), pageable, sysEnterpriseList.size());
        return Resp.success(pageData);
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
    public Resp detail(@RequestBody @Valid SysEnterprise post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("单位id不能为空");
        SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(post.getId()).orElse(null);
        if(sysEnterprise == null) return Resp.error("单位id无效");
        //List<SysEnterpriseMaterial> materialList = sysEnterpriseMaterialRepository.findAllByEnterpriseId(sysEnterprise.getId());
        //sysEnterprise.setMaterialList(materialList);
        //注：20230821新增注册账号的手机号，与注册人责任人联系电话不同（客服陈倩提出）
        SysUser sysUser = sysUserRepository.findById(sysEnterprise.getCreateUser()).orElse(null);
        if(!ObjectUtils.isEmpty(sysUser)) {
            sysEnterprise.setRegistPhone(sysUser.getPhone());
        }
        return Resp.success(sysEnterprise);
    }

    @PostMapping("/upload")
    public Resp upload(@RequestParam(defaultValue = "") String enterpriseId,
                           @RequestParam(defaultValue = "") String materialType,
                           @RequestParam("file") MultipartFile file) {
        //检查参数
        if(StringUtils.isEmpty(enterpriseId)) return Resp.error("单位id不能为空");
        if(StringUtils.isEmpty(materialType)) return Resp.error("材料类型不能为空");
        if(file.isEmpty()) return Resp.error("文件不能为空");
        if(file.getSize() > 1024*1024*2) return Resp.error("文件不能超过2M");
        String fileName = UUID.randomUUID().toString();
        if(file.getOriginalFilename().toLowerCase().endsWith(".jpg")) fileName = fileName + ".jpg";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".jpeg")) fileName = fileName + ".jpeg";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".png")) fileName = fileName + ".png";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".pdf")) fileName = fileName + ".pdf";
        else return Resp.error("上传文件仅支持jpg、png、pdf格式");
        //获取单位
        SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(enterpriseId).orElse(null);
        if(sysEnterprise == null) return Resp.error("单位id无效");
        //与前端约定：每次都可以修改
//        if(StatusEnum.SUBMIT.getCode().equals(sysEnterprise.getStatus())) return Resp.error("待审核状态，不能操作");
        if(!Oauth2Utils.getCurrentUser().getString("id").equals(sysEnterprise.getCreateUser())) return Resp.error("创建人不符，不能操作");
        //创建目录
        String path = "/enterprise-res/" + sysEnterprise.getSid() + "/";
        String folder = RES_PATH.replaceFirst("file:", "") + path;
        File targetFile = new File(folder);
        if (!targetFile.exists()) targetFile.mkdirs();
        //上传文件
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
        //保存文件
        String fileUrl = "/user" + path + fileName;
        if("EnterpriseLicensePic".equals(materialType)) sysEnterprise.setEnterpriseLicensePic(fileUrl);
        if("LegalIdCardPic".equals(materialType)) sysEnterprise.setLegalIdCardPic(fileUrl);
        if("RegisterIdCardPic".equals(materialType)) sysEnterprise.setRegisterIdCardPic(fileUrl);
        if("LegalAuthorizePic".equals(materialType)) sysEnterprise.setLegalAuthorizePic(fileUrl);
        sysEnterprise.setStatus(StatusEnum.EDIT.getCode()); //与前端约定：每次上传或修改改成EDIT
        sysEnterpriseRepository.save(sysEnterprise);
        return Resp.success(fileUrl);
    }

    /**
     * 审核（管理员）
     * @param post
     * @return
     */
    @PostMapping("/approval")
    public Resp approval(@RequestBody SysEnterprise post) {
        if(StringUtils.isEmpty(post.getId())) return Resp.error("单位id不能为空");
        if(StringUtils.isEmpty(post.getStatus())) return Resp.error("状态不能为空");
        SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(post.getId()).orElse(null);
        if(sysEnterprise == null) return Resp.error("单位id无效");
        if(!StatusEnum.SUBMIT.getCode().equals(sysEnterprise.getStatus())) return Resp.error("非待审核状态，不能操作");
        //更新审核记录
        SysApprovalRecord sysApprovalRecord = sysApprovalRecordRepository.findById(sysEnterprise.getApprovalRecordId()).orElse(null);
        if(ObjectUtils.isEmpty(sysApprovalRecord)) return Resp.error("审核记录id无效");
        sysApprovalRecord.setApprovalUserId(Oauth2Utils.getCurrentUser().getString("id"));
        sysApprovalRecord.setApprovalUserName(Oauth2Utils.getCurrentUser().getString("username"));
        sysApprovalRecord.setApprovalResult(post.getRemark());
        sysApprovalRecord.setApprovalTime(SqlUtils.getDateTime());
        sysApprovalRecord.setStatus(post.getStatus());
        sysApprovalRecord = sysApprovalRecordRepository.save(sysApprovalRecord);
        //更新单位状态
        sysEnterprise.setStatus(post.getStatus());
        sysEnterprise.setRemark(post.getRemark());
        return Resp.success(sysEnterpriseRepository.save(sysEnterprise));
    }

    /**
     * 添加单位角色
     * @param post
     * @return
     */
    @Transactional
    @PostMapping("/update/role")
    public Resp updateRole(@RequestBody SysEnterprise post) {
        if(!Oauth2Utils.getCurrentUser().getString("authorities").contains(RoleEnum.ROLE_ADMIN.getCode())) return Resp.error("不是管理员，无权操作");
        if(StringUtils.isEmpty(post.getId())) return Resp.error("id不能为空");
        if(StringUtils.isEmpty(post.getEnterpriseRole())) return Resp.error("角色不能为空");
        if(!RoleEnum.ROLE_AGENCY.getCode().equals(post.getEnterpriseRole())
                && !RoleEnum.ROLE_BIDDER.getCode().equals(post.getEnterpriseRole())
                && !post.getEnterpriseRole().equals("ROLE_AGENCY,ROLE_BIDDER")
                && !post.getEnterpriseRole().equals("ROLE_BIDDER,ROLE_AGENCY")) return Resp.error("角色无效");
        SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(post.getId()).orElse(null);
        if(ObjectUtils.isEmpty(sysEnterprise)) return Resp.error("id无效");
        sysEnterprise.setEnterpriseRole(post.getEnterpriseRole());
        sysEnterpriseRepository.save(sysEnterprise);
        List<SysUser> sysUserList = sysUserRepository.findByEnterpriseId(post.getId());
        for(SysUser sysUser : sysUserList) {
            sysUser.setRole(post.getEnterpriseRole());
            sysUserRepository.save(sysUser);
        }
        return Resp.success(sysEnterprise);
    }
}