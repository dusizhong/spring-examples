package com.dusizhong.examples.user.controller;

import com.dusizhong.examples.user.entity.SysUserInfoMaterial;
import com.dusizhong.examples.user.model.Resp;
import com.dusizhong.examples.user.repository.SysUserInfoMaterialRepository;
import com.dusizhong.examples.user.repository.SysUserRepository;
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

/**
 * 用户附件接口
 * @author Dusizhong
 * @since 2022-09-26
 */
@RestController
@RequestMapping("/user/material")
public class SysUserInfoMaterialController {

    @Value("${file.path}")
    private String FILE_PATH;
    @Value("${file.url}")
    private String FILE_URL;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserInfoMaterialRepository sysUserInfoMaterialRepository;

    /**
     * 上传附件
     * @param materialType
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Resp upload(@RequestParam(defaultValue = "") String materialType,
                       @RequestParam("file") MultipartFile file) {
        //检查参数
        if(StringUtils.isEmpty(materialType)) return Resp.error("附件类别不能为空");
        if(file.isEmpty()) return Resp.error("文件不能为空");
        if(file.getSize() > 1024*1024*1) return Resp.error("文件不能超过1M");
        String fileName = UUID.randomUUID().toString();
        if(file.getOriginalFilename().toLowerCase().endsWith(".jpg")) fileName = fileName + ".jpg";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".jpeg")) fileName = fileName + ".jpeg";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".png")) fileName = fileName + ".png";
        else if(file.getOriginalFilename().toLowerCase().endsWith(".pdf")) fileName = fileName + ".pdf";
        else return Resp.error("上传文件仅支持jpg、png、pdf格式");
        String userId = Oauth2Utils.getCurrentUser().getString("id");
        //创建路径
        String folder = FILE_PATH + "/users/";
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
        SysUserInfoMaterial material = sysUserInfoMaterialRepository.findByUserIdAndMaterialType(userId, materialType);
        if(material == null) {
            material = new SysUserInfoMaterial();
        }
        material.setUserId(userId);
        material.setMaterialType(materialType);
        material.setFileName(fileName);
        material.setFileAlias(file.getOriginalFilename());
        material.setFileType(file.getContentType());
        material.setFileSize(file.getSize());
        material.setFilePath(folder + fileName);
        material.setFileUrl(FILE_URL + "/users/" + fileName);
        return Resp.success(sysUserInfoMaterialRepository.save(material));
    }
}
