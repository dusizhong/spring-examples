package com.dusizhong.examples.file.util;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WatermarkUtils {

    /**
     * 给图片添加文字水印
     * @param srcImgPath 原图片位置
     * @param tarImgPath 加水印后图片输出位置
     * @param waterMarkContent 文字水印内容
     */
    public static void setTextToImage(String srcImgPath, String tarImgPath, String waterMarkContent) throws Exception {
        File srcImgFile = new File(srcImgPath);
        java.awt.Image srcImg = ImageIO.read(srcImgFile);
        int srcImgWidth = srcImg.getWidth(null);
        int srcImgHeight = srcImg.getHeight(null);
        // 加水印
        BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufImg.createGraphics();
        g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
        g.setColor(Color.BLUE);
        g.setFont(new Font("", Font.PLAIN, 18));
        //g.setFont(new Font("Calibri Light", Font.PLAIN, 24));
        g.rotate(Math.toRadians(0)); //水印旋转
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f)); //设置水印透明度
        //设置水印的坐标
        int textWidth = srcImgWidth - 330;
        int textHight = 60;
        g.drawString(waterMarkContent, textWidth, textHight);
        g.dispose();
        // 输出图片
        FileOutputStream outImgStream = new FileOutputStream(tarImgPath);
        ImageIO.write(bufImg, "jpg", outImgStream);
        outImgStream.flush();
        outImgStream.close();
    }

    /**
     * 给pdf添加文字水印
     * @param srcFile 原文件位置
     * @param destFile  加水印后文件位置
     * @param text  文字水印内容
     */
    public static void setTextToPdf(String srcFile, String destFile, String text)throws Exception {
        PdfReader reader = new PdfReader(srcFile);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(destFile));

        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.8f);// 设置透明度
        BaseFont font = BaseFont.createFont("Courier", "Cp1252", false);

        JLabel label = new JLabel();
        label.setText(text);
        FontMetrics metrics = label.getFontMetrics(label.getFont());
        int textH = metrics.getHeight() * 2;
        int textW = metrics.stringWidth(label.getText()) * 2;

        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;
        for (int i = 1; i < total; i++) {
            content = stamper.getOverContent(i);
            content.beginText();
            content.setGState(gs);

            content.setColorFill(BaseColor.BLUE);
            content.setFontAndSize(font, 18);

            // 开始写入水印
            float pageWidth = reader.getPageSize(i).getWidth();
            float pageHigh = reader.getPageSize(i).getHeight();
            content.showTextAligned(Element.ALIGN_TOP, text, 400, pageHigh - 30, 0);
            content.endText();
        }
        stamper.close();
    }

    /**
     * 给图片添加图片水印
     * @param srcImgPath 原图片位置
     * @param markImg 图片水印的位置
     * @param tarImgPath 加水印后图片位置
     */
    public static void setMarkToImage(String srcImgPath, String tarImgPath, String markImg, String markText) throws Exception{
        java.awt.Image img = ImageIO.read(new File(srcImgPath));
        java.awt.Image mark = ImageIO.read(new File(markImg));

        int imgWidth = img.getWidth(null);
        int imgHeight = img.getHeight(null);
        BufferedImage bufImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = bufImg.createGraphics();
        g.drawImage(img, 0, 0, bufImg.getWidth(), bufImg.getHeight(), null);
        g.rotate(Math.toRadians(39)); //水印旋转
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.1f)); //设置水印透明度
        //add mark image
        int markHeight = mark.getHeight(null);
        int markWidth = mark.getHeight(null);
        int interval = markWidth + markHeight; //行间隔
        for(int i=-imgHeight; i<imgWidth+imgHeight; i=i+interval+50){
            for(int j=-imgWidth; j<imgHeight+imgHeight; j=j+interval){
                g.drawImage(mark, i, j, null);
            }
        }

        //add mark text
        g.setColor(Color.BLUE);
        g.setFont(new Font("", Font.PLAIN, 18));
        //g.setFont(new Font("Calibri Light", Font.PLAIN, 24));
        g.rotate(Math.toRadians(-39)); //水印旋转
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f)); //设置水印透明度
        g.drawString(markText, 30, imgHeight - 30);

        g.dispose();
        FileOutputStream outImgStream = new FileOutputStream(tarImgPath);
        ImageIO.write(bufImg, "jpg", outImgStream);
        outImgStream.flush();
        outImgStream.close();
    }

    /**
     * 给pdf设置图片水印
     * @param sourceFilePath 原文件位置
     * @param targetFilePath 加水印后文件输出位置
     * @param markImg   水印位置
     */
    public static void setMarkToPdf(String sourceFilePath, String targetFilePath, String markImg, String markText) throws Exception {
        PdfReader reader = new PdfReader(sourceFilePath);
        PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(targetFilePath));

        Image mark = Image.getInstance(markImg);
        mark.scaleToFit(97, 33);
        mark.setRotationDegrees(321);
//        float markWidth = mark.getWidth();
//        float markHigh = mark.getHeight();

        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.1f);// 设置透明度

        BaseFont font = BaseFont.createFont("Courier", "Cp1252", false);

        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;
        for (int page = 1; page < total; page++) {
            content = stamp.getOverContent(page);
            content.setGState(gs);

            float pageWidth = reader.getPageSize(page).getWidth();
            float pageHigh = reader.getPageSize(page).getHeight();
            float interval = 58; //行间隔
            for(float i=-pageHigh; i<pageWidth+pageWidth; i=i+interval+50){
                for(float j=-pageWidth; j<pageHigh+pageHigh; j=j+interval){
                    // add mark image
                    mark.setAbsolutePosition(i, j);
                    content.addImage(mark);
                    //add mark text
                    content.beginText();
                    content.setColorFill(BaseColor.BLUE);
                    content.setFontAndSize(font, 16);
                    content.showTextAligned(Element.ALIGN_TOP, markText, 30, 30, 0);
                    content.endText();
                }
            }
        }
        stamp.close();
        reader.close();
    }

    /**获取文件类型*/
    public static String getFileType(String uri) throws IOException {
        Path path = Paths.get(uri);
        return Files.probeContentType(path);
    }

    public static void main(String[] args) {
        try {
            setMarkToImage("E:/guarantee.jpg","E:/guarantee1.jpg","E:/mark.png","No.123413243125131321231324567346");
            setTextToImage("E:/guarantee1.jpg", "E:/guarantee1.jpg","No.123413243125131321231324567346");
            setMarkToPdf("E:/guarantee.pdf","E:/guarantee1.pdf","E:/mark.png", "No.123413243125131321231324567346");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
