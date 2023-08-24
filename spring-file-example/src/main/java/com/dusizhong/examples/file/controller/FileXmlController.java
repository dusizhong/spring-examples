package com.dusizhong.examples.file.controller;

import com.dusizhong.examples.file.util.FileXmlUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
public class FileXmlController {

    @RequestMapping("/xmlToFile")
    public String xmlToFile(@RequestParam String filePath) {
        if(StringUtils.isEmpty(filePath)) return "生成文件路径不能为空";
        return FileXmlUtils.creatXmlFile(filePath);
    }

    @RequestMapping("/fileToXml")
    public String fileToXml(@RequestParam String filePath) {
        if(StringUtils.isEmpty(filePath)) return "文件路径不能为空";
        return FileXmlUtils.fileToXml(filePath);
    }
}
