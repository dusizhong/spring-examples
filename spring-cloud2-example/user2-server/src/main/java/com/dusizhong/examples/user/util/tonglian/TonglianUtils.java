package com.dusizhong.examples.user.util.tonglian;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通联统一支付工具类
 */
public class TonglianUtils {

    //统一支付中心地址
    public static String BASE_URL = "http://124.239.222.112:9010/PayCenterServer/allinpay";
    //app secret
    private static String appsecret = "eykbKrVudVANSpqiOhZK9w==";
    //allinpay code
    private static String allinpaycode = "wlgs2022";

    /**
     * 生成公共请求参数
     * @param bizUserId
     * @return
     * @throws Exception
     */
    public static JSONObject generalParams(String bizUserId) {

        String encryptedBizUserId = encryptBizUserId(bizUserId); //加密后bizUserId
        String timestamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        String sign = generalSign(encryptedBizUserId, timestamp); //生成的签名

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bizUserId", encryptedBizUserId);
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("sign", sign);
        return jsonObject;
    }

    /**
     * bizUserId加密
     * @param bizUserId
     * @return encryptedBizUserId
     * @throws Exception
     */
    public static String encryptBizUserId(String bizUserId) {
        byte[] bKey = Base64.decodeBase64(appsecret);
        byte[] sm4 = new byte[0];
        try {
            sm4 = SM4Util.encrypt_Ecb_Padding(bKey, bizUserId.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.encodeBase64String(sm4);
    }

    /**
     * bizUserId解密
     * @param encryptedBizUserId
     * @return bizUserId
     * @throws Exception
     */
    public static String decryptBizUserId(String encryptedBizUserId) {
        byte[] bizUserId = Base64.decodeBase64(encryptedBizUserId);
        byte[] bKey = Base64.decodeBase64(appsecret);
        byte[] byteStr = new byte[0];
        try {
            byteStr = SM4Util.decrypt_Ecb_Padding(bKey, bizUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Util.byteToString(byteStr);
    }

    /**
     * 生成签名sign（timestamp：yyMMddHHmmss）
     * @param encryptedBizUserId
     * @param timestamp
     * @return sign
     * @throws Exception
     */
    public static String generalSign(String encryptedBizUserId, String timestamp) {
        byte[] hash = new byte[0];
        try {
            hash = SM3Util.hash((encryptedBizUserId + allinpaycode + timestamp + appsecret).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder signature = new StringBuilder();
        for (byte b : hash) {
            signature.append(Util.byteToHexString(b));
        }
        return signature.toString();
    }

    /**
     * 验签
     * @param encryptedBizUserId
     * @param timestamp
     * @return
     */
    public static Boolean verifySign(String encryptedBizUserId, String timestamp, String sign) {
        Boolean result = false;
        String mySign = generalSign(encryptedBizUserId, timestamp);
        if(mySign.equals(sign)) {
            result = true;
        }
        return result;
    }

    /**
     * 生成订单号
     * （生成规则：3位平台编号 + 17位时间戳（yyyyMMddHHmmssSSS）+ 10位随机数字）
     * @return
     */
    public static String generalOrderNo() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String random = RandomStringUtils.random(10, false, true);
        return "100" + timestamp + random;
    }

    public static void main(String[] args) {
//        String bizUserId = "3KiGg+zQ1+YjZ3mhCbbw7Yp1yoUBzkuZiFH13/1gCbguKZI5kdUloIx3Phpvj/5p";
//        String timestamp = "220414085546";
//        String sign = "d38a090da480590ecdb98d07955facbd7ec66e44a71c8713050947add9528136";
//        String signAcctName = "10001公司";

        String bizUserId = "d264e43b-7d18-42ee-8d39-e19ee85aa000";
        String encryptedBizUserId = encryptBizUserId(bizUserId);
        System.out.println(encryptedBizUserId);
        String timestamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        System.out.println(timestamp);
        System.out.println(generalSign(encryptedBizUserId, timestamp));

        System.out.println(generalOrderNo());
    }
}
