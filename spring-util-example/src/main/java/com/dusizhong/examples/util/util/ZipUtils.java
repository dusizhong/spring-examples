package com.dusizhong.examples.util.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件打包工具类
 */
public class ZipUtils {

    /**
     * 压缩前的文件校验和压缩后压缩文件的构件
     * @param path   要压缩的文件路径
     * @param format 生成的格式（zip、rar）
     */
    public static void generateFile(String path, String format) throws Exception {

        File file = new File(path);
        // 压缩文件的路径不存在
        if (!file.exists()) {
            throw new Exception("路径 " + path + " 不存在文件，无法进行压缩...");
        }
        // 用于存放压缩文件的文件夹
        File compress = new File(file.getParent());
        // 如果文件夹不存在，进行创建
        if( !compress.exists() ) {
            //compress.mkdirs();
            throw new Exception("异常! 获取存放压缩文件的文件夹失败" + file.getParent());
        }
        // 目的压缩文件
        String absolutePath = compress.getAbsolutePath();
        // 定义压缩操作后，压缩文件的名称，本次采取  文件名XXX.XXX 的形式进行定义(可以自定义)
        String generateFileName = absolutePath + File.separator + file.getName() + "." + format;
        // 压缩文件数据的输出流：用于将压缩文件的数据存储至指定的压缩文件中
        FileOutputStream outputStream = new FileOutputStream(generateFileName);

        // 压缩输出流
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
        zipFile(zipOutputStream,file,"");

        //System.out.println("源文件位置：" + file.getAbsolutePath() + "，目的压缩文件生成位置：" + generateFileName);
        // 关闭 输出流
        zipOutputStream.close();
    }

    /**
     * 主要的打包压缩操作
     * @param out  输出流
     * @param file 目标文件
     * @param dir  文件夹
     * @throws Exception
     */
    private static void zipFile(ZipOutputStream out, File file, String dir) throws Exception {

        // 如果是文件夹，则采取递归方式继续检索，获取到最终的文件为止，当然这里有性能问题，文件足够大会性能降低
        if (file.isDirectory()) {
            //得到文件列表信息
            File[] files = file.listFiles();

            // 将文件夹添加到下一级打包目录
            // 这里的打包目录必须是每个新的目录(或文件)都需要额外创建新的 ZipEntry 对象
            out.putNextEntry(new ZipEntry(dir + File.separator));

            dir = dir.length() == 0 ? "" : dir + File.separator;

            // 文件夹，则采取递归继续检索，直到识别是文件为止
            for (int i = 0; i < files.length; i++) {
                zipFile(out, files[i], dir + files[i].getName());
            }

            return;
        }

        // 当识别到的 File 对象 是文件时，执行下列逻辑
        // 将文件信息，以流的形式读取到内存中
        FileInputStream inputStream = new FileInputStream(file);
        // 每个新的文件，都需要额外创建一个新的  ZipEntry
        // 将需要打包的文件，放置于新的条目中
        out.putNextEntry(new ZipEntry(dir));

        // 将文件流中的数据信息，分段读取并写入至对应的输出流中
        int len = 0;
        byte[] bytes = new byte[1024];
        while ((len = inputStream.read(bytes)) > 0) {
            out.write(bytes, 0, len);
        }

        // 关闭输入流
        inputStream.close();
    }

//    public static void main(String[] args) {
//        String path = "/nginx/html/hbxf-res/HBXF2022-06-004G";
//        String format = "zip";
//
//        try {
//            generateFile(path, format);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//        }
//    }
}
