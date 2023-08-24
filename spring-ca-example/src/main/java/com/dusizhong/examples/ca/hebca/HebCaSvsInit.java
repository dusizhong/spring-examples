package com.dusizhong.examples.ca.hebca;

import com.hebca.pki.HebcaVerify;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class HebCaSvsInit implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments applicationArguments) {
        try {
            HebcaVerify.init("/home/HebcaSvs.properties");
            System.out.println("HebcaSvs init success");
        } catch (Exception e) {
            System.out.println("HebcaSvs init failed: " + e.getMessage());
        }
    }

    public static boolean verifySign(String cert, String data, String sign) {
        try {
            HebcaVerify.getInstance().verifyCertSign(data, cert, sign);
            return true;
        } catch (Exception e) {
            System.err.println("CA验签失败");
            return false;
        }
    }

    public static String getUniqueId(String cert) {
        try {
            return HebcaVerify.getInstance().getCertSubjectUniqueId(cert);
        } catch (Exception e) {
            System.err.println("获取证书唯一项失败");
            return null;
        }
    }

    public static String getSerialNumber(String cert) {
        try {
            return HebcaVerify.getInstance().getCertSn(cert);
        } catch (Exception e) {
            System.err.println("获取证书序列号失败");
            return null;
        }
    }

    public static String getSubject(String cert) {
        try {
            return HebcaVerify.getInstance().getCertSubjectDN(cert);
        } catch (Exception e) {
            System.err.println("获取证书主体失败");
            return null;
        }
    }

    public static String getIssuer(String cert) {
        try {
            return HebcaVerify.getInstance().getInsure(cert);
        } catch (Exception e) {
            System.err.println("获取证书颁发者失败");
            return null;
        }
    }

//    public static String unEnvelop(String cert, String pkcs7Data) {
//        try {
//            return HebcaVerify.getInstance().getHelper().PKCS7UnEnvelop(cert, pkcs7Data, (THostInfoSt)null);
//        } catch (Exception e) {
//            System.err.println("获取证书颁发者失败");
//            return null;
//        }
//    }
}
