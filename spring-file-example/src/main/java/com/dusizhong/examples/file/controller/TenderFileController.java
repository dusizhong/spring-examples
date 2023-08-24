package com.dusizhong.examples.file.controller;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/tenderFile")
public class TenderFileController {

    @RequestMapping("/pack")
    public String pack(@RequestParam("file") MultipartFile file) {

        String base64File = "";
        try {
            byte[] bytes = file.getBytes();
            base64File = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fileName = file.getOriginalFilename().substring(0,file.getOriginalFilename().lastIndexOf("."));

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("root");

        Element tenderInfo = root.addElement("tenderInfo");
        tenderInfo.addElement("tenderProjectName").addText("中国测试集团有限公司人工智能测试系统招标项目");
        tenderInfo.addElement("tenderProjectNo").addText("ZG2023-001");
        tenderInfo.addElement("tenderSectionName").addText("人工智能测试系统招标项目001标段");
        tenderInfo.addElement("tenderSectionNo").addText("ZG2023-001-001");
        tenderInfo.addElement("tendereeName").addText("中国测试集团有限公司");
        tenderInfo.addElement("agencyName").addText("中国测试代理有限公司");

        Element tenderForm = root.addElement("tenderForm");
        tenderForm.addElement("formField")
                .addAttribute("fieldId", "1").addAttribute("fieldName", "报价")
                .addAttribute("fieldType", "number").addAttribute("fieldLength", "10")
                .addAttribute("fieldNote", "单位元");
        tenderForm.addElement("formField")
                .addAttribute("fieldId", "2").addAttribute("fieldName", "工期")
                .addAttribute("fieldType", "text").addAttribute("fieldLength", "4")
                .addAttribute("fieldNote", "日历天");
        tenderForm.addElement("formField")
                .addAttribute("fieldId", "3").addAttribute("fieldName", "质量要求")
                .addAttribute("fieldType", "text").addAttribute("fieldLength", "100")
                .addAttribute("fieldNote", "请填写质量说明");

        Element tenderFile = root.addElement("tenderFile");
        tenderFile.addAttribute("fileName", fileName).addText(base64File);

        try {
            FileWriter out = new FileWriter("/home/" + fileName + ".tdf");
            document.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "打包成功";
    }

    @RequestMapping("/unpack")
    public String unpack(@RequestParam("file") MultipartFile file) {

        String xml = "";
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(file.getInputStream());
            Element root = document.getRootElement();

            Element tenderInfo = root.element("tenderInfo");
            Iterator<Element> ti = tenderInfo.elementIterator();
            while (ti.hasNext()) {
                Element element = ti.next();
                xml = xml + element.getName() + ": " + element.getText() + "\n";
            }

            Element tenderForm = root.element("tenderForm");
            Iterator<Element> tf = tenderForm.elementIterator();
            while (tf.hasNext()) {
                Element element = tf.next();
                xml = xml + element.getName() + ": " + element.getText() + "\n";
                List<Attribute> attrs = element.attributes();
                for(Attribute attr: attrs) {
                    xml = xml + "(数据项属性：" + attr.getName() + " = " + attr.getValue() + ")\n";
                }
            }

            Element tenderFile = root.element("tenderFile");
            String base64File = tenderFile.getText();
            String targetPath = "/home/" + tenderFile.attribute("fileName").getValue() + "[解包].pdf";
            Files.write(Paths.get(targetPath), Base64.getDecoder().decode(base64File), StandardOpenOption.CREATE);
            xml = xml + "解包成功：" + targetPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }
}
