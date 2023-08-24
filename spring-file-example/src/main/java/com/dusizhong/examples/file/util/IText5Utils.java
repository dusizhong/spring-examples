package com.dusizhong.examples.file.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
public class IText5Utils {

    private static String FILE_PATH = "/home/";

//    @Value("${file.path}")
//    public void setPath(String path) {
//        IText5Utils.FILE_PATH = path;
//    }

    public static String htmlToPdf(String html, String fileName) {
        try {
            String path = FILE_PATH + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "/";
            File folder = new File(path);
            if (!folder.exists()) folder.mkdirs();
            String filePath = path + fileName;
            Document document = new Document(PageSize.A4);
            OutputStream outputStream = new FileOutputStream(filePath);
            PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
            document.open();
            XMLWorkerHelper xmlWorkerHelper = XMLWorkerHelper.getInstance();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            InputStream cssInputStream = null;
            xmlWorkerHelper.parseXHtml(pdfWriter, document, byteArrayInputStream, cssInputStream, StandardCharsets.UTF_8, new FontProvider());
            document.close();
            outputStream.flush();
            outputStream.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static class FontProvider extends XMLWorkerFontProvider {
        @Override
        public Font getFont(final String fontName, final String encoding, final boolean embedded, final float size, final int style, final BaseColor color) {
            BaseFont bf = null;
            try {
                //String fontPath = ResourceUtils.getURL("classpath:").getPath() + "static/simsun.ttf";
                String fontPath = "/home/simsun.ttf";
                bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font font = new Font(bf, size, style, color);
                font.setColor(color);
                return font;
            } catch (Exception e) {
                System.err.println("IText5Utils:" + e);
            }
            return null;
        }
    }
}
