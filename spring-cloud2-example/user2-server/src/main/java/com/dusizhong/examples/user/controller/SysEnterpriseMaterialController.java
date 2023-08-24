package com.dusizhong.examples.user.controller;

import com.dusizhong.examples.user.entity.SysEnterprise;
import com.dusizhong.examples.user.entity.SysEnterpriseMaterial;
import com.dusizhong.examples.user.enums.StatusEnum;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.SysEnterpriseMaterialRepository;
import com.dusizhong.examples.user.repository.SysEnterpriseRepository;
import com.dusizhong.examples.user.util.Oauth2Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

///**
// * 单位附件接口
// * @author Dusizhong
// * @since 2022-09-22
// */
@RestController
@RequestMapping("/enterprise/material")
public class SysEnterpriseMaterialController {

    @Value("${file.path}")
    private String FILE_PATH;
    @Value("${file.url}")
    private String FILE_URL;
    @Autowired
    private SysEnterpriseRepository sysEnterpriseRepository;
    @Autowired
    private SysEnterpriseMaterialRepository sysEnterpriseMaterialRepository;

    /**
     * 上传附件
     * @param materialType
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Resp upload(@RequestParam(defaultValue = "") String enterpriseId,
                       @RequestParam(defaultValue = "") String materialType,
                       @RequestParam("file") MultipartFile file) {
        //检查参数
        if(StringUtils.isEmpty(enterpriseId)) return Resp.error("单位id不能为空");
        if(StringUtils.isEmpty(materialType)) return Resp.error("附件类别不能为空");
        if(file.isEmpty()) return Resp.error("文件不能为空");
        if(file.getSize() > 1024*1024*1) return Resp.error("文件不能超过1M");
        String fileName = UUID.randomUUID().toString();
        if(file.getOriginalFilename().toLowerCase().endsWith(".jpg")) fileName = fileName + ".jpg";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".jpeg")) fileName = fileName + ".jpeg";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".png")) fileName = fileName + ".png";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".pdf")) fileName = fileName + ".pdf";
        else return Resp.error("上传文件仅支持jpg、png、pdf格式");
        //获取单位
        SysEnterprise sysEnterprise = sysEnterpriseRepository.findById(enterpriseId).orElse(null);
        if(sysEnterprise == null) return Resp.error("单位id无效");
        if(StatusEnum.SUBMIT.getCode().equals(sysEnterprise.getStatus())) return Resp.error("待审核状态，不能操作");
        if(!Oauth2Utils.getCurrentUser().getString("id").equals(sysEnterprise.getCreateUser())) return Resp.error("创建人不符，不能操作");
        //创建路径
        String folder = FILE_PATH + sysEnterprise.getSid() + "/";
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
        //保存附件
        SysEnterpriseMaterial material = sysEnterpriseMaterialRepository.findByEnterpriseIdAndMaterialType(enterpriseId, materialType);
        if(material == null) {
            material = new SysEnterpriseMaterial();
        }
        material.setEnterpriseId(enterpriseId);
        material.setMaterialType(materialType);
        material.setFileName(fileName);
        material.setFileAlias(file.getOriginalFilename());
        material.setFileType(file.getContentType());
        material.setFileSize(file.getSize());
        material.setFilePath(folder + fileName);
        material.setFileUrl(FILE_URL + sysEnterprise.getSid() + "/" + fileName);
        material = sysEnterpriseMaterialRepository.save(material);
        return Resp.success(material);
    }
}
