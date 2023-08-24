package com.dusizhong.examples.ca.hebca;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;

@RestController
@RequestMapping("/hebca")
public class HebCaSealController {

    @CrossOrigin("*")
    @PostMapping("/saveNetFile")
    public String saveNetFile(@RequestParam("filepdf") MultipartFile file,
                              @RequestParam(name = "fileName", defaultValue = "") String fileName) {
        if(file.isEmpty()) return "文件不能为空";
        String filePath = "/home/" + fileName + System.currentTimeMillis() + ".pdf";
        FileOutputStream out;
        try {
            out = new FileOutputStream(filePath);
            out.write(file.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }
}
