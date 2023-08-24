package com.dusizhong.examples.encrypt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class PKIUtils {

    private static final Logger log = LoggerFactory.getLogger(PKIUtils.class);

    /**
     *
     * 用证书的私钥签名
     * @param in 证书库
     * @param storePassword 证书库密码
     * @param keyPassword 证书密码
     * @param key 钥别名
     * @param data 待签名数据
     * @return 签名
     */
    public static byte[] signature(InputStream in, String storePassword, String keyPassword, String key, byte[] data) {
        try {
            // 获取证书私钥
            PrivateKey privateKey = getPrivateKey(in, storePassword, keyPassword, key);
            Signature signet = Signature.getInstance("MD5withRSA");
            signet.initSign(privateKey);
            signet.update(data);
            byte[] signed = signet.sign(); //对信息的数字签名
            return signed;
        } catch (Exception ex) {
            log.error("签名失败",ex);
        }
        return null;
    }

    /**
     * 用证书的公钥验证签名
     * @param in 证书
     * @param data 原始数据
     * @param signatureData 对原始数据的签名
     * @return
     */
    public static boolean verifySignature(InputStream in, byte[] data, byte[] signatureData){
        try {
            // 获取证书公钥
            PublicKey key = getPublicKey(in);
            Signature signet = Signature.getInstance("MD5withRSA");
            signet.initVerify(key);
            signet.update(data);
            boolean result=signet.verify(signatureData);
            return result;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            log.error("验证签名失败",ex);
        }
        return false;
    }

    /**
     * 获取证书公钥
     * @param in 证书
     * @return 公钥
     */
    private static PublicKey getPublicKey(InputStream in) {
        try {
            // 用证书的公钥加密
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate cert = factory.generateCertificate(in);
            // 得到证书文件携带的公钥
            PublicKey key = cert.getPublicKey();
            return key;
        } catch (CertificateException ex) {
            log.error("获取证书公钥失败",ex);
        }
        return null;
    }

    /**
     * 加密数据
     * @param key 公钥或私钥
     * @param data 待加密数据
     * @return
     */
    public static byte[] encrypt(Key key, byte[] data) {
        try {
            // 定义算法：RSA
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            // 正式执行加密操作
            byte encryptedData[] = cipher.doFinal(data);
            return encryptedData;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            log.error("加密数据失败",ex);
        }
        return null;
    }
    /**
     * 用证书的公钥加密
     * @param in 证书
     * @param data 待加密数据
     * @return 密文
     */
    public static byte[] encryptWithPublicKey(InputStream in, byte[] data) {
        try {
            // 获取证书公钥
            PublicKey key = getPublicKey(in);

            byte encryptedData[] = encrypt(key,data);
            return encryptedData;
        } catch (Exception ex) {
            log.error("用证书的公钥加密失败",ex);
        }
        return null;
    }
    /**
     * 用证书的私钥加密
     * @param in 证书库
     * @param storePassword 证书库密码
     * @param keyPassword 证书密码
     * @param key 钥别名
     * @param data 待加密数据
     * @return 密文
     */
    public static byte[] encryptWithPrivateKey(InputStream in, String storePassword, String keyPassword, String key, byte[] data) {
        try {
            // 获取证书私钥
            PrivateKey privateKey = getPrivateKey(in, storePassword, keyPassword, key);

            byte encryptedData[] = encrypt(privateKey,data);
            return encryptedData;
        } catch (Exception ex) {
            log.error("用证书的私钥加密失败",ex);
        }
        return null;
    }

    /**
     * 获取证书私钥
     * @param in 证书库
     * @param storePassword 证书库密码
     * @param keyPassword 证书密码
     * @param key 钥别名
     * @return 私钥
     */
    private static PrivateKey getPrivateKey(InputStream in, String storePassword, String keyPassword, String key) {
        try {
            // 加载证书库
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(in, storePassword.toCharArray());
            // 获取证书私钥
            PrivateKey privateKey = (PrivateKey) ks.getKey(key, keyPassword.toCharArray());
            return privateKey;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException ex) {
            log.error("获取证书私钥失败",ex);
        }
        return null;
    }

    /**
     * 解密数据
     * @param key 公钥或私钥
     * @param data 待解密数据
     * @return  明文
     */
    public static byte[] decrypt(Key key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            // 解密后的数据
            byte[] result = cipher.doFinal(data);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            log.error("解密数据失败",ex);
        }
        return null;
    }
    /**
     *
     * 用证书的私钥解密
     * @param in 证书库
     * @param storePassword 证书库密码
     * @param keyPassword 证书密码
     * @param key 钥别名
     * @param data 待解密数据
     * @return 明文
     */
    public static byte[] decryptWithPrivateKey(InputStream in, String storePassword, String keyPassword, String key, byte[] data) {
        try {
            // 获取证书私钥
            PrivateKey privateKey = getPrivateKey(in, storePassword, keyPassword, key);
            // 解密后的数据
            byte[] result = decrypt(privateKey,data);
            return result;
        } catch (Exception ex) {
            log.error("用证书的私钥解密失败",ex);
        }
        return null;
    }
    /**
     *
     * 用证书的公钥解密
     * @param in 证书
     * @param data 待解密数据
     * @return 明文
     */
    public static byte[] decryptWithPublicKey(InputStream in, byte[] data) {
        try {
            // 获取证书公钥
            PublicKey key = getPublicKey(in);
            // 解密后的数据
            byte[] result = decrypt(key,data);
            return result;
        } catch (Exception ex) {
            log.error("用证书的公钥解密失败",ex);
        }
        return null;
    }


    public void genkey() {
//        String[] arstringCommand = new String[] {
//                "cmd ", "/k",
//                "start", // cmd Shell命令
//                "G:\\java\\bin\\keytool",
//                "-genkey", // -genkey表示生成密钥
//                "-validity", // -validity指定证书有效期(单位：天)，这里是36500天
//                "36500",
//                "-keysize",//     指定密钥长度
//                "1024",
//                "-alias", // -alias指定别名，这里是ss
//                "ss",
//                "-keyalg", // -keyalg 指定密钥的算法 (如 RSA DSA（如果不指定默认采用DSA）)
//                "RSA",
//                "-keystore", // -keystore指定存储位置，这里是d:/demo.keystore
//                caUrl,
//                "-dname",// CN=(名字与姓氏), OU=(组织单位名称), O=(组织名称), L=(城市或区域名称),
//                // ST=(州或省份名称), C=(单位的两字母国家代码)"
//                "CN=("+caCn+"), OU=("+caOu+"), O=("+caO+"), L=("+caL+"),ST=("+caSt+"), C=("+caC+")",
//                "-storepass", // 指定密钥库的密码(获取keystore信息所需的密码)
//                "123456",
//                "-keypass",// 指定别名条目的密码(私钥的密码)
//                caKeypass,
//                "-v"// -v 显示密钥库中的证书详细信息
//        };

        //execCommand(arstringCommand);
    }

    public void execCommand(String[] arstringCommand) {
        for (int i = 0; i < arstringCommand.length; i++) {
            System.out.print(arstringCommand[i] + " ");
        }
        try {
            Runtime.getRuntime().exec(arstringCommand);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void execCommand(String arstringCommand) {
        try {
            Runtime.getRuntime().exec(arstringCommand);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {

        //private String cert = "/com/apdplat/module/security/pki/apdplat_public.crt";
        String cert = "/com/apdplat/module/security/pki/apdplat.crt";
        String store = "/com/apdplat/module/security/pki/apdplat.keystore";
        String plainText = "apdplat应用级开发平台";
//        public void testEncryptAndDecrypt1() {
//            //公钥加密
//            byte[] result = PKIService.encryptWithPublicKey(PKIService.class.getResourceAsStream(cert), plainText.getBytes());
//
//            //私钥解密
//            result = PKIService.decryptWithPrivateKey(PKIService.class.getResourceAsStream(store), "apdplat_core_module", "apdplat_core_module", "apdplat", result);
//
//            Assert.assertEquals(plainText, new String(result));
//        }
//        public void testEncryptAndDecrypt2() {
//            //私钥加密
//            byte[] result = PKIUtils.encryptWithPrivateKey(PKIUtilsTest.class.getResourceAsStream(store), "apdplat_core_module", "apdplat_core_module", "apdplat", plainText.getBytes());
//
//            //公钥解密
//            result = PKIUtils.decryptWithPublicKey(PKIUtilsTest.class.getResourceAsStream(cert), result);
//
//            Assert.assertEquals(plainText, new String(result));
//        }
//
//        public void testSignatureAndVerifySignature() {
//            //私钥签名
//            byte[] signature = PKIUtils.signature(PKIUtilsTest.class.getResourceAsStream(store), "apdplat_core_module", "apdplat_core_module", "apdplat", plainText.getBytes());
//
//            //公钥验证签名
//            boolean correct=PKIUtils.verifySignature(PKIUtilsTest.class.getResourceAsStream(cert),plainText.getBytes(),signature);
//
//            Assert.assertTrue(correct);
//        }
    }
}
