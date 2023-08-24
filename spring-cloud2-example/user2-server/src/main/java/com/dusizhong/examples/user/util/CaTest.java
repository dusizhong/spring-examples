package com.dusizhong.examples.user.util;

import com.cwca.certServer.api.CWCAUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;


public class CaTest {

    //公钥证书
    public static String pubCert = "MIICeDCCAhugAwIBAgIIEAAAAAEcXMgwDAYIKoEcz1UBg3UFADAfMQswCQYDVQQGEwJDTjEQMA4GA1UEAwwHY2EtdGVzdDAeFw0yMjA0MTIwMjAzMjlaFw0yMzA0MjkwMjAzMjlaMCcxGDAWBgNVBAMMD3NtMuWPjOivgea1i+ivlTELMAkGA1UEBhMCQ04wWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAATf3WOfs6INUyAgwprQI6k8OY1ud+65ib9R582aL7WzBvMy52bqmdWR23NLEJF+9ZGPCM/NduoswwcMcbvL6jhBo4IBNTCCATEwHQYDVR0OBBYEFELn3TTiaog5hHsAxSFfmYnTKxcqMB8GA1UdIwQYMBaAFNTXpDtpij7sT5SZ45BX3mfhqY79MAsGA1UdDwQEAwIGwDCB0wYDVR0fBIHLMIHIMGOgPKA6hjhsZGFwOi8vMjAyLjEwMC4xMDguNDA6Mzg5L2NuPWZ1bGxDcmwuY3JsLENOPWNhLXRlc3QsQz1DTqIjpCEwHzELMAkGA1UEBhMCQ04xEDAOBgNVBAMMB2NhLXRlc3QwYaA6oDiGNmh0dHA6Ly8yMjIuNzUuMTY3LjE1NDo4MTgxL3Rlc3RjYS8xMDAwMDAwMDAxMUM1QzAwLmNybKIjpCEwHzELMAkGA1UEBhMCQ04xEDAOBgNVBAMMB2NhLXRlc3QwDAYDVR0TBAUwAwEBADAMBggqgRzPVQGDdQUAA0kAMEYCIQCqg7zrgPqu44f3Gakxxpkk0KXAv1xfqgp2NjfAJiM3uQIhAOV71mPNX85GxybW4Wwl+xgjNfgbPUgZhsElspBQN9HC";
    //签名原文
    public static String data = "34324234234sdfoeuoaojf";
    //签名值
    public static String signedValue = "MEQCIHFSk5AemQ1MzYwyXeyPKg+XSAUXz3kXHwlJaLidX1GwAiBIqe87vkAYfJ4FL6ZRyOuvaLH2UIXS2JR5vZNvjvAgmg==";

    //测试公钥证书（海泰key）
    public static String pubCert1 = "MIIC1jCCAj+gAwIBAgIIOpgGpxU4fF0wDQYJKoZIhvcNAQEFBQAwUDENMAsGA1UE" +
            "AxMETlhDQTENMAsGA1UEChMEQ1dDQTERMA8GA1UEBxMIWWluY2h1YW4xEDAOBgNV" +
            "BAgTB05pbmd4aWExCzAJBgNVBAYTAkNOMB4XDTIyMDgyNjAzMTI1NVoXDTIzMDgy" +
            "NjAzMTI1NVowfzEVMBMGA1UEAx4MXeV6C21Li9VigGcNMREwDwYDVQQKHghtS4vV" +
            "bNVOujENMAsGA1UEBx4ElPZd3TENMAsGA1UECB4EW4FZDzENMAsGA1UECxMER0da" +
            "WTEZMBcGA1UEDBMQNTI0RDM0MzYzMzAzODE1MDELMAkGA1UEBhMCQ04wgZ8wDQYJ" +
            "KoZIhvcNAQEBBQADgY0AMIGJAoGBAMMiwl2b6SUUjOaOITl8f/6raZ6cCw45M4ZY" +
            "SE9DaeLAhjJrjjyr43WmtRVz2LKSCa6eSz8GXYtKTCMQGk2xSOjWSM0iP0PsAzdJ" +
            "GNinCnfcbqKMVeiYEq8rWv2P09CNVtNnMnCkhe8ajqaA1NL3cz+IQUBr0H9vIQHU" +
            "gXr9IN6rAgMBAAGjgYkwgYYwHwYDVR0jBBgwFoAUE/odJdUa1n+H6kGQYKy0n/Ha" +
            "T3wwNwYDVR0fBDAwLjAsoCqgKIYmaHR0cDovLzIwMi4xMDAuMTA4LjY6ODA4MC9j" +
            "cmwvY3JsMS5jcmwwCwYDVR0PBAQDAgQwMB0GA1UdDgQWBBQRnPmO5NthCAUUF6hC" +
            "oW3YLidnfjANBgkqhkiG9w0BAQUFAAOBgQBT30Qaf2F5mR0+gSALBcTn/sxdZVDD" +
            "EYzuXI0yOQqAIG6/KaHYx/yd81d3aFYwCyw1SxajNZbOW3msGs/tJzuxA18pSotS" +
            "tumcXrLjqBf9IJyy8nYmA3uIPeGZkjKwOnzQOmw4QiQkdw1JVzsRbmJzCXyaO/4Q" +
            "neZ/she6tsYrjg==";

    //加密后对称密钥数字信封（海泰key）
    public static String envelop = "MIIBWwYJKoZIhvcNAQcDoIIBTDCCAUgCAQAxgfQwgfECAQAwXDBQMQ0wCwYDVQQDEwROWENBMQ0wCwYDVQQKEwRDV0NBMREwDwYDVQQHEwhZaW5jaHVhbjEQMA4GA1UECBMHTmluZ3hpYTELMAkGA1UEBhMCQ04CCDqYBqcVOHxdMAsGCSqGSIb3DQEBAQSBgCeDpa1yb2/WsYu9ri0PLFKEOt8wrV+DzSI2PtmsXPFzCLmozOl66VmAVW/HFfu2T8LvSl0CnisBmaAVUQvCVqkSdVBcYG8aVf4gOppHTOcO/oytxYdm4psVrgsAs7vHMDEJyBOrNQmfNGrAfjILOSLMREFh9bL1k5Ssv2vHObX/MEwGCSqGSIb3DQEHATAdBglghkgBZQMEAQIEEOrPrJzBg/MGbDTx95+Db2yAIO3Jk2I/hy8Zw3A0XHTCiAOxd7a0CS25YZcYQW3OqghO";
    //对称密钥（海泰key）
    public static String aesKey = "3oBobDnllIll/Hd/OWY2jg==";

    //测试用公钥证书（龙脉key）
    public static String pubCert2 = "MIIDtDCCA1egAwIBAgIIEAAAAALdIegwDAYIKoEcz1UBg3UFADBQMQswCQYDVQQG" +
            "EwJDTjEQMA4GA1UECAwHTmluZ3hpYTERMA8GA1UEBwwIWWluY2h1YW4xDTALBgNV" +
            "BAoMBENXQ0ExDTALBgNVBAMMBE5YQ0EwHhcNMjIwODI2MDMyNDA3WhcNMjMwODI0" +
            "MDMyNDA3WjCBhjELMAkGA1UEBhMCQ04xDzANBgNVBAgMBuWugeWkjzEPMA0GA1UE" +
            "BwwG6ZO25bedMSgwJgYDVQQKDB8xfDEyMzQ1Njc4OTBJ5rWL6K+V5Liq5Lq65ZCN" +
            "56ewMQ4wDAYDVQQLDAUwMDF8UDEbMBkGA1UEAwwS5rWL6K+V5Liq5Lq65ZCN56ew" +
            "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEePfDIRGHAgUb8KuRJU8ciJ8u/SvX" +
            "IMjule3REq2/FsSDQLw3VmClBA07ZAkKBLHH5ZbwxQ4skmfCK5hC3JEKvaOCAeAw" +
            "ggHcMB0GA1UdDgQWBBS4JPTUPOHovFZf6WaBnCNbuxPMEjAfBgNVHSMEGDAWgBQa" +
            "UUYQLj4n/Y0JC6HiYd0woT9IrTALBgNVHQ8EBAMCBsAwHQYDVR0lBBYwFAYIKwYB" +
            "BQUHAwIGCCsGAQUFBwMEMIIBXgYDVR0fBIIBVTCCAVEwgbugY6Bhhl9sZGFwOi8v" +
            "MjAyLjEwMC4xMDguMTM6Mzg5L2NuPWZ1bGxDcmwuY3JsLENOPU5YQ0FfTERBUCxP" +
            "VT1OWENBLE89Q1dDQSxMPVlpbmNodWFuLFNUPU5pbmd4aWEsQz1DTqJUpFIwUDEL" +
            "MAkGA1UEBhMCQ04xEDAOBgNVBAgMB05pbmd4aWExETAPBgNVBAcMCFlpbmNodWFu" +
            "MQ0wCwYDVQQKDARDV0NBMQ0wCwYDVQQDDAROWENBMIGQoDigNoY0aHR0cDovLzIw" +
            "Mi4xMDAuMTA4LjE1OjgxODEvbnhjYS8xMDAwMDAwMDAyREQyMTAwLmNybKJUpFIw" +
            "UDELMAkGA1UEBhMCQ04xEDAOBgNVBAgMB05pbmd4aWExETAPBgNVBAcMCFlpbmNo" +
            "dWFuMQ0wCwYDVQQKDARDV0NBMQ0wCwYDVQQDDAROWENBMAwGA1UdEwQFMAMBAQAw" +
            "DAYIKoEcz1UBg3UFAANJADBGAiEA7nZFx4ggwc7gqeprl/mgfk4DhMW/tHN38Gm+" +
            "Bw2JU18CIQDFdjDNxDpC5l24lwx+2q65zdnxwrT/5haDQWx/G/cR1w==";

    //测试验签
    public static int sm2VerifySigned() {
        com.aisino.svs.Sign testSig = new com.aisino.svs.Sign();
        testSig.init("/NixcaSvs.properties");
        int value = testSig.P1Verify(pubCert, data.getBytes(), signedValue);
        System.out.println(value); //0验证通过，其他失败
        return value;
    }

    //测试解析证书（由于海泰key有两个容器，获取的序列号为加密证书的序列号）
    public static void getCertInfo() {
        com.aisino.svs.Sign testSig = new com.aisino.svs.Sign();
        testSig.init("/NixcaSvs.properties");
        com.aisino.svs.CertInfo certInfo = testSig.ParseCert(pubCert2);
        System.out.println("证书序列号：" + certInfo.sn);
        System.out.println("证书主体：" + certInfo.subject);
        System.out.println("证书颁发者：" + certInfo.issuer);
        System.out.println("证书生效时间：" + certInfo.beginTime);
        System.out.println("证书失效时间：" + certInfo.endTime);
        System.out.println("签名算法：" + certInfo.algo);
        System.out.println("签名算法位数：" + certInfo.bits);
    }

    //测试服务端解密投标文件
    public static void decryptFile() throws Exception {
        //前端加密
        //1.前端产生base64编码的对称密钥 cwcaClient.SOF_GenRandom(16);
        //2.前端用对称密钥加密文件 cwcaClient.SOF_SymEncryptFile(base64SymKey, inFilePath, outFilePath);
        //3.前端用加密证书公钥加密对称密钥 cwcaClient.SOF_EncryptData(encCert, base64SymKey);
        //4.前端将对称密钥和加密后文件发给服务端?
        //AES解密文件
        CWCAUtils cwcaUtils = new CWCAUtils();
        cwcaUtils.SOF_setEncryptMethod(0x00004002L);
        boolean e = cwcaUtils.SOF_decryptFile(aesKey, "D://西部CA测试文件（密文）.nxtb", "D://西部CA测试文件（服务端解密）.zip");
        System.out.println("AES解密结果：" + e);
    }


    /** -----------------------------自定义实现解析公钥证书信息-----------------------------------*/

    /**
     * 获取证书序列号
     * （由于海泰key有两个容器，获取的序列号为加密证书的序列号）
     * @param pubCert
     * @return
     */
    public static String getCertSerialNumber(String pubCert) {
        return getX509Cert(pubCert).getSerialNumber().toString(16);
    }

    /**
     * 获取证书主体信息
     * @param pubCert
     * @return
     */
    public static String getCertSubjectDN(String pubCert) {
        return getX509Cert(pubCert).getSubjectDN().getName();
    }

    /**
     * 获得证书颁发者信息
     * @param pubCert
     * @return
     */
    public static String getCertIssueDN(String pubCert) {
        return getX509Cert(pubCert).getIssuerDN().getName();
    }

    /**
     * 获取证书生效日期
     * @param pubCert
     * @return
     */
    public static String getCertBeforeDate(String pubCert) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beforeDate = getX509Cert(pubCert).getNotBefore();
        return dateFormat.format(beforeDate);
    }

    /**
     * 获取证书失效日期
     * @param pubCert
     * @return
     */
    public static String getCertAfterDate(String pubCert) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date afterDate = getX509Cert(pubCert).getNotAfter();
        return dateFormat.format(afterDate);
    }

    /**
     * 获取证书签名算法
     * @param pubCert
     * @return
     */
    public static String getCertSigAlgName(String pubCert) {
        return getX509Cert(pubCert).getSigAlgName();
    }

    /**
     * 获取证书版本
     * @param pubCert
     * @return
     */
    public static String getCertVersion(String pubCert) {
        return String.valueOf(getX509Cert(pubCert).getVersion());
    }


    /**
     * 获取签名公钥
     * （注：海泰key由于有两个容器，实际获取的为加密公钥）
     * @param pubCert
     * @return
     */
    public static String getPubKey(String pubCert) {
        PublicKey pk = getX509Cert(pubCert).getPublicKey();
        BASE64Encoder bse = new BASE64Encoder();
        System.out.println("Base64公钥信息:\n"+bse.encode(pk.getEncoded()));
        return getX509Cert(pubCert).getSigAlgOID();
    }

    //获取证书对象
    private static X509Certificate getX509Cert(String pubCert) {
        X509Certificate oCert = null;
        try {
            //读取证书文件
            //InputStream inputStream = new FileInputStream("/宁夏个人锁国密公钥证书.cer");
            //InputStream inputStream = new FileInputStream("/宁夏海泰公钥证书.cer");
            //读取Base64证书
            InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(pubCert));
            //引入BC库 证书类型是SM2证书时使用
            Security.addProvider(new BouncyCastleProvider());
            //创建X509工厂类
            CertificateFactory cf = CertificateFactory.getInstance("X.509","BC"); //SM2证书时使用
            //CertificateFactory cf = CertificateFactory.getInstance("X.509"); //RSA证书时使用
            //创建证书对象
            oCert = (X509Certificate) cf.generateCertificate(inputStream);
            inputStream.close();
        } catch (Exception e) {
            System.out.println("解析证书出错！");
            e.printStackTrace();
        }
        return oCert;
    }

    public static void main(String[] args) throws Exception {
        //sm2VerifySigned();
        //getCertInfo();
        //decryptFile();

        //海泰key
        String cert = "MIIC1jCCAj+gAwIBAgIIJUbgCydF53MwDQYJKoZIhvcNAQEFBQAwUDENMAsGA1UE" +
                "AxMETlhDQTENMAsGA1UEChMEQ1dDQTERMA8GA1UEBxMIWWluY2h1YW4xEDAOBgNV" +
                "BAgTB05pbmd4aWExCzAJBgNVBAYTAkNOMB4XDTIyMDgyNjAzMTI1NVoXDTIzMDgy" +
                "NjAzMTI1NVowfzEVMBMGA1UEAx4MXeV6C21Li9VigGcNMREwDwYDVQQKHghtS4vV" +
                "bNVOujENMAsGA1UEBx4ElPZd3TENMAsGA1UECB4EW4FZDzENMAsGA1UECxMER0da" +
                "WTEZMBcGA1UEDBMQNTI0RDM0MzYzMzAzODE1MDELMAkGA1UEBhMCQ04wgZ8wDQYJ" +
                "KoZIhvcNAQEBBQADgY0AMIGJAoGBANbTrB9CgkjWCcVlxXC3Wpb34+aFZr9ZPIlr" +
                "TQOf5DrK3aPnwA7IwmID47SvZkC2AoOHq0qafM5Co6fyQeBZ5+gbkMs46AcAfUAA" +
                "uLB94s6HkCt7SdWP0kZwDas5utiwntqjVOqT50rb4eiXe1y1V6n3tyMJZO+1OSlR" +
                "Bppd/WILAgMBAAGjgYkwgYYwHwYDVR0jBBgwFoAUE/odJdUa1n+H6kGQYKy0n/Ha" +
                "T3wwNwYDVR0fBDAwLjAsoCqgKIYmaHR0cDovLzIwMi4xMDAuMTA4LjY6ODA4MC9j" +
                "cmwvY3JsMS5jcmwwCwYDVR0PBAQDAgbAMB0GA1UdDgQWBBR0eej57e1iwutYpoSB" +
                "BKSUBeZmzzANBgkqhkiG9w0BAQUFAAOBgQA0QbpXpFRjT15BwiG/wE/kdNjnze+y" +
                "QuXGJpsheiM/QWa4cZW6O/lm9xuocR7crw2g6OiFFJFZGZ2wJaXbnGStLfjlWMuN" +
                "+JvJ2q0xej61IFMM+L12r7k/33XEHrI7RAyJwTfKCroXs334j1KzSLWmk8NWbWvX" +
                "fgy6mAGLZ2p/BA==";
        String random = "34324234234sdfoeuoaojf";
        String sign = "NSzaPQfRTWf/f+hXhN5trxhaw1Lho7P4EE/DMAZl3FwQ7zQRRFe7DkQZmiDMnmT1" +
                "tOmOaM2tYxXxi4Wx5nXiH6EFtLJxp4VRA6XySlhUPBvmZtWDMHBUgXKFbiGjNfMA" +
                "33uQqQG46cA+13CbNVUrW1VbKOplDf1bVKDNCctpv9Y=";

        System.out.println(getCertSerialNumber(cert));
        System.out.println(getCertSubjectDN(cert));

        com.aisino.svs.Sign testSig = new com.aisino.svs.Sign();
        testSig.init("/NixcaSvs.properties");
        int value = testSig.P1Verify(cert, random.getBytes(), sign);
        System.out.println(value); //0验证通过，其他失败
    }
}
