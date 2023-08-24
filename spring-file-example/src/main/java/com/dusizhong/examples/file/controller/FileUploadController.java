package com.dusizhong.examples.file.controller;

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

@RestController
@RequestMapping("/file")
public class FileUploadController {

//    @Value("${file.path}")
//    private String FILE_PATH;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, @RequestParam String fileName) {
        if(StringUtils.isEmpty(fileName)) return "文件名不能为空";
        if(file.isEmpty()) return "文件不能为空";
        String path = "/home/";
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String filePath = path + fileName;
        FileOutputStream out;
        try {
            out = new FileOutputStream(filePath, true); //将分片文件追加保存，结合前端适用于大文件分片上传
            out.write(file.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "获取文件失败";
        } catch (IOException e) {
            return "上传失败";
        }
        return filePath;
    }
}
