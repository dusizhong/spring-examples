package com.dusizhong.examples.file.controller;

import com.dusizhong.examples.file.util.IText2Utils;
import com.dusizhong.examples.file.util.IText5Utils;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

@RestController
public class FilePdfController {

    @RequestMapping("/htmlToPdf")
    public String htmlToPdf() {
        String html = "<p><span style=\"font-family: Microsoft YaHei;\">微软雅黑: 粗体前A<strong>A粗体A</strong>A粗体后</span></p>\n" +
                "<p><span style=\"font-family: SimSun;\">宋体: 粗体前A<strong>A粗体A</strong>A粗体后</span></p>\n" +
                "<p><span style=\"font-family: STHeiti;\">黑体: 粗体前A<strong>A粗体A</strong>A粗体后</span></p>" +
                "<p><span style=\"font-family: Times New Roman;\">Times New Roman: pre bdA<strong>AbdA</strong>Aaft bd</span></p>\n";
        return IText5Utils.htmlToPdf(html, "html.pdf");
    }
}
