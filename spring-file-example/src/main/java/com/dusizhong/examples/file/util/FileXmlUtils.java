package com.dusizhong.examples.file.util;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class FileXmlUtils {

    public static String creatXmlFile(String filePath) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("root");
        Element author1 = root.addElement("author")
                .addAttribute("name", "James")
                .addAttribute("location", "UK")
                .addText("James Strachan");
        Element author2 = root.addElement("author")
                .addAttribute("name", "Bob")
                .addAttribute("location", "US")
                .addText("Bob McWhirter");

        FileWriter out = null;
        try {
            out = new FileWriter(filePath);
            document.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "创建成功";
    }

    public static String fileToXml(String filePath) {
        String xml = "";
        SAXReader saxReader = new SAXReader();
        try {
            //URL url = new URL("http://oss-cn-shenzhen.aliyuncs.com/recording-to-text/text/372169067517181952.xml");
            //Document document = saxReader.read(url);
            Document document = saxReader.read(new File(filePath));
            Element rootElement = document.getRootElement();
            Iterator<Element> it = rootElement.elementIterator();
            while (it.hasNext()) {
                Element element = it.next();
                xml = xml + ("数据项：" + element.getName() + " = " + element.getText()) + "\n";

                List<Attribute> attrs = element.attributes();
                for(Attribute attr: attrs) {
                    xml = xml + ("数据项属性：" + attr.getName() + " = " + attr.getValue()) + "\n";
                }

                Iterator cit = element.elementIterator();
                while (cit.hasNext()) {
                    Element child = (Element) cit.next();
                    xml = xml + ("数据项子节点：" + child.getName()) + "\n";
                }
                xml = xml + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }
}
