package com.dusizhong.examples.file.controller;

import com.dusizhong.examples.file.util.FileBase64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
public class FileBase64Controller {

    @RequestMapping("/fileToBase64")
    public String fileToBase64(@RequestParam String filePath) {
        if(StringUtils.isEmpty(filePath)) return "文件路径不能为空";
        return FileBase64Utils.fileToBase64(filePath);
    }

    @RequestMapping("/base64ToFile")
    public String base64ToFile(@RequestParam String base64, @RequestParam String filePath) {
        if(StringUtils.isEmpty(base64)) return "base64字符串不能为空";
        if(StringUtils.isEmpty(filePath)) return "生成文件路径不能为空";
        return FileBase64Utils.base64ToFile(base64, filePath);
    }
}
