package com.dusizhong.examples.ca.hebca;

import com.dusizhong.examples.ca.hebca.util.SM4Util;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

@RestController
@RequestMapping("/hebca")
public class HebcaEnvelopController {

    private static final String SYS_KEY = "88d20410b39b4fa6851c56582191a001"; //系统密钥，用于与客户端约定加密整个文件（暂未使用）

    @CrossOrigin("*")
    @RequestMapping("/getSecretKey")
    public String getSecretKey(HttpServletRequest request) {
        String bdfFilePath = "C:\\Users\\Administrator\\Desktop\\采购项目招标文件2投标文件.bdf";
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new File(bdfFilePath));
            Element root = document.getRootElement();
            return root.element("SecretKey").getText();
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    @CrossOrigin("*")
    @PostMapping("/decryptText")
    public String decryptText(@RequestParam String secretKey) {
        secretKey = new String(Base64.getDecoder().decode(secretKey));
        System.out.println("secretKey: " + secretKey);
        String bdfFilePath = "C:\\Users\\Administrator\\Desktop\\采购项目招标文件2投标文件.bdf";
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new File(bdfFilePath));
            Element root = document.getRootElement();
            String cipherData = root.element("tenderForm").getText();
//            String base64Text = SM4Util.decryptECB(secretKey, cipherText);
//            System.out.println("base64Text " + base64Text);
//            return new String(Base64.getDecoder().decode(base64Text));
            byte[] plainData = SM4Util.decryptECB(secretKey, cipherData);
            return new String(plainData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @CrossOrigin("*")
    @PostMapping("/decryptFile")
    public String decryptFile(@RequestParam String secretKey) {
        secretKey = new String(Base64.getDecoder().decode(secretKey));
        String bdfFilePath = "C:\\Users\\Administrator\\Desktop\\采购项目招标文件2投标文件.bdf";
        String targetPath = "C:\\Users\\Administrator\\Desktop\\采购项目招标文件2投标文件[解密].pdf";
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new File(bdfFilePath));
            Element root = document.getRootElement();
            String cipherBidFile = root.element("bidFile").getText();
            byte[] byteBidFile = SM4Util.decryptECB(secretKey, cipherBidFile);
            //Files.write(Paths.get(targetPath), Base64.getDecoder().decode(base64BidFile), StandardOpenOption.CREATE);
            Files.write(Paths.get(targetPath), byteBidFile, StandardOpenOption.CREATE);
            return "解密成功";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
