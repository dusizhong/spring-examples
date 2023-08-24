package com.dusizhong.examples.user.config.custom;

import com.hebca.pki.HebcaVerify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 河北CA启动器
 * @author Dusizhong
 * @since 2022-09-22
 */
@Slf4j
@Component
public class HebcaSvsInit implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments applicationArguments) {
        try {
            String path = "/usr/local/ezjc-cloud/HebcaSvs.properties";
            HebcaVerify.init(path);
            log.info("HebcaSvs init success");
        } catch (Exception e) {
            log.info("HebcaSvs init failed: " + e.getMessage());
        }
    }

    /**
     * CA验签
     * @param cert
     * @param data
     * @param sign
     * @return
     */
    public static boolean verifySign(String cert, String data, String sign) {
        try {
            HebcaVerify.getInstance().verifyCertSign(data, cert, sign);
            return true;
        } catch (Exception e) {
            log.info("CA验签失败");
            return false;
        }
    }

    /**
     * 获取证书唯一项
     * @param cert
     * @return
     */
    public static String getUniqueId(String cert) {
        try {
            return HebcaVerify.getInstance().getCertSubjectUniqueId(cert);
        } catch (Exception e) {
            log.info("获取证书唯一项失败");
            return null;
        }
    }

    /**
     * 获取证书序列号
     * @param cert
     * @return
     */
    public static String getSerialNumber(String cert) {
        try {
            return HebcaVerify.getInstance().getCertSn(cert);
        } catch (Exception e) {
            log.info("获取证书序列号失败");
            return null;
        }
    }

    /**
     * 获取证书主体
     * @param cert
     * @return
     */
    public static String getSubject(String cert) {
        try {
            return HebcaVerify.getInstance().getCertSubjectDN(cert);
        } catch (Exception e) {
            log.info("获取证书主体失败");
            return null;
        }
    }

    /**
     * 获取证书颁发者
     * @param cert
     * @return
     */
    public static String getIssuer(String cert) {
        try {
            return HebcaVerify.getInstance().getInsure(cert);
        } catch (Exception e) {
            log.info("获取证书颁发者失败");
            return null;
        }
    }

    /**
     * 获取证书生效时间
     * @param cert
     * @return
     */
    public static String getBeginTime(String cert) {
        return null;
    }

    /**
     * 获取证书失效时间
     * @param cert
     * @return
     */
    public static String getEndTime(String cert) {
        return null;
    }

    /**
     * 获取证书签名算法
     * @param pubCert
     * @return
     */
    public static String getAlgorithm(String pubCert) {
        return null;
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
        System.out.println("证书唯一项：" + getUniqueId(pubCert2));
        System.out.println("证书序列号：" + getSerialNumber(pubCert2));
        System.out.println("证书主体：" + getSubject(pubCert2));
        System.out.println("证书颁发者：" + getIssuer(pubCert2));
//        System.out.println("证书生效日期：" + getBeginTime(pubCert2));
//        System.out.println("证书失效日期：" + getEndTime(pubCert2));
//        System.out.println("证书签名算法：" + getAlgorithm(pubCert2));
        try {
            System.out.println("caKey " +  HebcaVerify.getInstance().getCertSubjectUniqueId(pubCert2)); //证书唯一项
            System.out.println("serialNo " + HebcaVerify.getInstance().getCertSn(pubCert2)); //证书序列号
            System.out.println("enterpriseName " + HebcaVerify.getInstance().getSubjectItem(pubCert2, 0));
            System.out.println("enterpriseUnifiedCode " + HebcaVerify.getInstance().getSubjectItem(pubCert2, 8));
        } catch (Exception e) {
            log.info("CA验签失败");
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}

