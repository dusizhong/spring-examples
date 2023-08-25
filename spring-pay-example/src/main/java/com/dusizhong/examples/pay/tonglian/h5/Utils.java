package com.dusizhong.examples.pay.tonglian.h5;

import com.alibaba.fastjson.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {

    /**
     * js转化为实体
     * @param <T>
     * @param jsonstr
     * @param cls
     * @return
     */
    public static <T> T json2Obj(String jsonstr,Class<T> cls){
        JSONObject jo =JSONObject.parseObject(jsonstr);
        T obj = (T)JSONObject.toJavaObject(jo, cls);
        return obj;
    }

    /**
     * 32位 md5
     * @param b
     * @return
     */
    public static String md5(byte[] b) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(b);
            byte[] hash = md.digest();
            StringBuffer outStrBuf = new StringBuffer(32);
            for (int i = 0; i < hash.length; i++) {
                int v = hash[i] & 0xFF;
                if (v < 16) {
                    outStrBuf.append('0');
                }
                outStrBuf.append(Integer.toString(v, 16).toLowerCase());
            }
            return outStrBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new String(b);
        }
    }

    /**
     * 判断字符串是否为空
     * @param s
     * @return
     */
    public static boolean isEmpty(String s){
        if(s==null||"".equals(s.trim()))
            return true;
        return false;
    }

    /**
     * 生成随机码
     * @param n
     * @return
     */
    public static String getRandom(int n) {
        Random random = new Random();
        String sRand = "";
        n = n == 0 ? 4 : n;// default 4
        for (int i = 0; i < n; i++) {
            String rand = String.valueOf(random.nextInt(10));
            sRand += rand;
        }
        return sRand;
    }
}
