package com.dusizhong.examples.pay.hebpay;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static com.dusizhong.examples.pay.wxpay.XmlUtils.xmlToMap;

/**
 * 河北银行聚合支付工具
 */
public class HebPayUtils {

    /**
     * 生成签名
     * （签名原始串中，字段名和字段值都采用原始值，不进行URL Encode）
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static String generateSignature(final Map<String, String> data, String key) throws Exception {
        // ascii码从小到大排序
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) { //sign字段，不参与签名
                continue;
            }
            if (data.get(k).trim().length() > 0) { //参数值为空，不参与签名
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
            }
        }
        sb.append("key=").append(key);
        return MD5(sb.toString()).toUpperCase();
    }

    /**
     * 验证签名
     * @param xmlStr
     * @param key
     * @return
     * @throws Exception
     */
    public static boolean isSignatureValid(String xmlStr, String key) throws Exception {
        Map<String, String> data = xmlToMap(xmlStr);
        if (!data.containsKey("sign") ) {
            return false;
        }
        String sign = data.get("sign");
        return generateSignature(data, key).equals(sign);
    }

    /**
     * MD5
     * @param data
     * @return
     * @throws Exception
     */
    private static String MD5(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 32位随机字符串
     * @return
     */
    public static String generateNonceStr() {
        String SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random RANDOM = new SecureRandom();
        char[] nonceChars = new char[32];
        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
        }
        return new String(nonceChars);
    }
}
