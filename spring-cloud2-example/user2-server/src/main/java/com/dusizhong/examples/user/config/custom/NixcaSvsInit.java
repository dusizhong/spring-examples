package com.dusizhong.examples.user.config.custom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

/**
 * 宁夏CA启动器
 * @author Dusizhong
 * @since 2022-09-13
 */
@Slf4j
//@Component //暂时停用
public class NixcaSvsInit implements ApplicationRunner {

    private static com.aisino.svs.Sign svsSign = null;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(svsSign == null) {
            String path = "./NixcaSvs.properties";
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                path = ResourceUtils.getURL("classpath:").getPath() + "NixcaSvs.properties";
            }
            svsSign = new com.aisino.svs.Sign();
            svsSign.init(path);
        }
        log.info("NixcaSvs init success");
    }

    private static com.aisino.svs.Sign getInstance() {
        return svsSign;
    }

    /**
     * SM2验签
     * @param cert
     * @param data
     * @param sign
     * @return
     */
    public static boolean verifySign(String cert, String data, String sign) {
        if(getInstance().P1Verify(cert, data.getBytes(), sign) == 0) {
            return true;
        } else return false;
    }

    /**
     * 获取证书序列号
     * （由于海泰key有两个容器，获取的序列号为加密证书的序列号）
     * @param cert
     * @return
     */
    public static String getSerialNumber(String cert) {
        com.aisino.svs.CertInfo certInfo = getInstance().ParseCert(cert);
        return certInfo.sn;
    }

    /**
     * 获取证书主体
     * @param pubCert
     * @return
     */
    public static String getSubject(String pubCert) {
        com.aisino.svs.CertInfo certInfo = getInstance().ParseCert(pubCert);
        return certInfo.subject;
    }

    /**
     * 获取证书颁发者
     * @param pubCert
     * @return
     */
    public static String getIssuer(String pubCert) {
        com.aisino.svs.CertInfo certInfo = getInstance().ParseCert(pubCert);
        return certInfo.issuer;
    }

    /**
     * 获取证书生效时间
     * @param pubCert
     * @return
     */
    public static String getBeginTime(String pubCert) {
        com.aisino.svs.CertInfo certInfo = getInstance().ParseCert(pubCert);
        return certInfo.beginTime;
    }

    /**
     * 获取证书失效时间
     * @param pubCert
     * @return
     */
    public static String getEndTime(String pubCert) {
        com.aisino.svs.CertInfo certInfo = getInstance().ParseCert(pubCert);
        return certInfo.endTime;
    }

    /**
     * 获取证书签名算法
     * @param pubCert
     * @return
     */
    public static String getAlgorithm(String pubCert) {
        com.aisino.svs.CertInfo certInfo = getInstance().ParseCert(pubCert);
        return certInfo.algo;
    }

    /**
     * 获取证书签名算法位数
     * @param pubCert
     * @return
     */
    public static String getBit(String pubCert) {
        com.aisino.svs.CertInfo certInfo = getInstance().ParseCert(pubCert);
        return certInfo.bits;
    }

    //测试用公钥证书（海泰key）
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

    public static void main(String[] args) {

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
        
//        com.aisino.svs.Sign svsSign = new com.aisino.svs.Sign();
//        svsSign.init("D:/NixcaSvs.properties");
//        System.out.println(svsSign.P1Verify(cert, random.getBytes(), sign));

        System.out.println("证书序列号：" + getSerialNumber(pubCert1));
        System.out.println("证书主体：" + getSubject(cert));
        System.out.println("证书颁发者：" + getIssuer(pubCert2));
        System.out.println("证书生效日期：" + getBeginTime(pubCert2));
        System.out.println("证书失效日期：" + getEndTime(pubCert2));
        System.out.println("证书签名算法：" + getAlgorithm(pubCert2));
        System.out.println("证书签名算法位数：" + getBit(pubCert2));
    }
}
