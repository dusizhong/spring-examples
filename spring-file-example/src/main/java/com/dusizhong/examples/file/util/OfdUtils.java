package com.dusizhong.examples.file.util;

import org.ofdrw.layout.OFDDoc;
import org.ofdrw.layout.element.Paragraph;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OfdUtils {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/HelloWorld.ofd");
        try (OFDDoc ofdDoc = new OFDDoc(path)) {
            Paragraph p = new Paragraph("你好呀，OFD Reader&Writer！");
            ofdDoc.add(p);
        }
        System.out.println("生成文档位置: " + path.toAbsolutePath());

    }
}