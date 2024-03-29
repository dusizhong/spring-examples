package com.dusizhong.examples.util.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * 文件类型工具
 */
public class FileTypeUtils {

    // 缓存文件头信息-文件头信息
    public static final HashMap<String, String> mFileTypes = new HashMap<>();

    static {
        // file types
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("504B0304", "docx");
        //mFileTypes.put("D0CF11E0", "xls");//excel2003版本文件
        //mFileTypes.put("504B0304", "xlsx");//excel2007以上版本文件
        mFileTypes.put("255044462D312E", "pdf"); //pdf
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("504B0304 ", "zip");
    }

    /**
     * @param filePath 文件路径
     * @return 文件头信息
     * 根据文件路径获取文件头信息
     */
    public static String getFileType(String filePath) {
        return mFileTypes.get(getFileHeader(filePath));
    }

    /**
     * @param filePath 文件路径
     * @return 文件头信息
     * 根据文件路径获取文件头信息
     */
    public static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[4];
            /*
             * int read() 从此输入流中读取一个数据字节。int read(byte[] b) 从此输入流中将最多 b.length
             * 个字节的数据读入一个 byte 数组中。 int read(byte[] b, int off, int len)
             * 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
             */
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    /**
     * @param src 要读取文件头信息的文件的byte数组
     * @return 文件头信息
     * 方法描述：将要读取文件头信息的文件的byte数组转换成string类型表示
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
//      System.out.println(builder.toString());
        return builder.toString();
    }


    /**
     * @param args
     * @throws Exception
     * 方法描述：测试
     */
    public static void main(String[] args) throws Exception {
        final String fileType = getFileType("C:\\Users\\Administrator\\Desktop\\logo.jpg");
        System.out.println(fileType);
    }
}
