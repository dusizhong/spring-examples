package com.dusizhong.examples.encrypt.util;

import org.bouncycastle.util.encoders.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HmacMD5 {

    public static void main(String[] args)throws Exception{
        String key = "yqRAryH8pmjurS8lxFHNENQFShUh/BturiiZIHfxfLAjuicdhi8fm0260dbkVsc5XVEzlhfEiJuUWD0z0g4o0Q==";
        String str = "idCard=130105197811270628&phone=13730426199&platformCode=I1301000075&time=1684228708125&userName=尹玉";
        //获取秘钥
        String keyB64=genKeyB64();
        System.out.println("秘钥为："+key);
        //计算摘要
        String ori="hello world";
        String digestMstB64=getMsg(key,str);
        System.out.println("摘要为："+digestMstB64);
        //验证摘要信息
        boolean check=checkMsg(str,digestMstB64,key);
        System.out.println("验证结果为："+check);
    }

    /**
     * 根据原文、摘要、秘钥验证签名是否正确
     * @param ori
     * @param msgB64
     * @param keyB64
     * @return
     */
    public static boolean checkMsg(String ori,String msgB64,String keyB64){
        try{
            msgB64=msgB64.replace(" ","+");
            SecretKey secretKey=new SecretKeySpec(Base64.decode(keyB64),"HmacMD5");
            Mac mac= Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            String checkMsgB64= Base64.toBase64String(mac.doFinal(ori.getBytes("utf-8")));
            if(msgB64.equals(checkMsgB64)){
                return true;
            }else{
                return false;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 根据秘钥和原文计算摘要
     * @param keyB64
     * @param ori
     * @return
     * @throws RuntimeException
     */
    public static String getMsg(String keyB64,String ori)throws RuntimeException {
        try{
            SecretKey secretKey=new SecretKeySpec(Base64.decode(keyB64),"HmacMD5");
            Mac mac= Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return Base64.toBase64String(mac.doFinal(ori.getBytes("utf-8")));
        }catch (Exception ex){
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * 生成Base64格式的秘钥字符串
     * @return
     * @throws Exception
     */
    public static String genKeyB64()throws Exception{
        // 生成密钥
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacMD5");
        SecretKey key = keyGen.generateKey();
        byte[] keyBytes=key.getEncoded();
        return Base64.toBase64String(keyBytes);
    }
}
