package com.dusizhong.examples.user.util.joint;

import com.dusizhong.examples.user.util.tonglian.SM3Util;
import com.dusizhong.examples.user.util.tonglian.Util;

import java.io.UnsupportedEncodingException;

public class SignUtils {

    private static String appsecret = "asfdsasaddsdsa";

    public static String generalSign(String str) {
        byte[] hash = new byte[0];
        try {
            hash = SM3Util.hash((str + appsecret).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder signature = new StringBuilder();
        for (byte b : hash) {
            signature.append(Util.byteToHexString(b));
        }
        return signature.toString();
    }
}
