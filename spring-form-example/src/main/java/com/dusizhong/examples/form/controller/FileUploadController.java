package com.dusizhong.examples.form.controller;

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

@RestController
@RequestMapping("/file")
public class FileUploadController {

    //@Value("${spring.resources.static-locations}")
    //private String RES_PATH;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, @RequestParam String memo) {
        if(file.isEmpty()) return "文件不能为空";
        String fileName = file.getOriginalFilename();
        System.out.println(fileName);
        fileName = UUID.randomUUID() + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
        //String targetPath = RES_PATH.replaceFirst("file:", "") + "/res/";
        String targetPath = "/temp/res/";
        File folder = new File(targetPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(targetPath + fileName);
            out.write(file.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "获取文件失败";
        } catch (IOException e) {
            return "上传失败";
        }
        return fileName;
    }
}
