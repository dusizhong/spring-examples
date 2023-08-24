package com.dusizhong.examples.ca.hebca;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hebca")
public class HebcaEncryptController {

    @CrossOrigin("*")
    @PostMapping("/encrypt")
    public String encrypt(@RequestParam(name = "filePath", defaultValue = "") String filePath) {
        if(filePath.isEmpty()) return "文件路径不能为空";
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }
}
