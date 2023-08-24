package com.dusizhong.examples.ca.hebca;

import com.hebca.pki.Cert;
import com.hebca.pki.CertParse;
import com.hebca.svs.client.SvsException;
import com.hebca.svs.client.SvsResult;
import com.hebca.svs.client.cluster.SvsClusterClientHelper;
import com.hebca.svs.client.st.*;
import com.hebca.svs.client.util.MyBase64;
import com.hebca.util.RandomGen;
import org.springframework.util.ResourceUtils;
import org2.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.Security;
import java.util.*;

/**
 * test only
 * not use it
 */
public class HebCaTest {
    private static HebCaTest verify = null;
    private static final String BCProviderName = "BC2";
    private SvsClusterClientHelper helper = new SvsClusterClientHelper();
    private RandomGen r = new RandomGen();
    private String caName;
    private String svsErrorMode;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private HebCaTest(Properties config) {
        this.helper.initialize(config);
        this.caName = config.getProperty("verify.caName");
        this.svsErrorMode = config.getProperty("svs.error.mode");
    }

    private HebCaTest(String configFile) throws Exception {
        Properties p = new Properties();
        p.load(new FileInputStream(configFile));
        this.helper.initialize(p);
        this.caName = p.getProperty("verify.caName");
        this.svsErrorMode = p.getProperty("svs.error.mode");
    }

    private HebCaTest() throws Exception {
        Properties pro = new Properties();

        try {
            FileInputStream in = new FileInputStream(URLDecoder.decode(this.getClass().getResource("/").getPath(), "UTF-8") + "/HebcaSvs.properties");
            pro.load(in);
            in.close();
        } catch (IOException var7) {
            try {
                FileInputStream in = new FileInputStream(URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "UTF-8") + "/HebcaSvs.properties");
                pro.load(in);
                in.close();
            } catch (Exception var6) {
                throw new Exception("HebcaSvs.properties is not exist!");
            }
        }

        try {
            this.helper.initialize(pro);
        } catch (Exception var5) {
            throw new Exception("HebcaSvs initialized error!");
        }

        try {
            this.caName = pro.getProperty("verify.caName");
            this.svsErrorMode = pro.getProperty("svs.error.mode");
        } catch (Exception var4) {
            throw new Exception("HebcaSvs.properties caName or error.mode is not exist!");
        }
    }

    public SvsClusterClientHelper getHelper() {
        return this.helper;
    }

    public static void init(Properties config) {
        if (verify == null) {
            verify = new HebCaTest(config);
        }

    }

    public static void init(String configFile) throws Exception {
        if (verify == null) {
            verify = new HebCaTest(configFile);
        }

    }

    public static void init() throws Exception {
        if (verify == null) {
            verify = new HebCaTest();
        }

    }

    public static void destroy() {
        if (verify != null) {
            verify.getHelper().finalize();
            verify = null;
        }

    }

    public static HebCaTest getInstance() throws Exception {
        return verify;
    }

    private boolean isExceptionMode() {
        return this.svsErrorMode != null && this.svsErrorMode.equals("exception");
    }

    private int handleSvsResult(SvsResult r) throws Exception {
        if (this.isExceptionMode()) {
            if (r.isError()) {
                throw new SvsException(r.getResult(), r.getMessage());
            } else {
                return r.getResult();
            }
        } else {
            return r.getResult();
        }
    }

    public int autoDataVerify(HttpServletRequest request) throws Exception {
        String signFlag = request.getParameter("_HebcaSignFlag");
        if (signFlag == null) {
            return this.handleSvsResult(SvsResult.CLIENT_ERROR);
        } else if ("P1".equals(signFlag)) {
            String _Hebca_Original_Text = request.getParameter("_Hebca_Original_Text");
            if (_Hebca_Original_Text == null) {
                return this.handleSvsResult(SvsResult.CLIENT_ERROR);
            } else {
                _Hebca_Original_Text = _Hebca_Original_Text.replaceAll("%2B", "+");
                String _Hebca_SignCert = request.getParameter("_Hebca_SignCert");
                if (_Hebca_SignCert == null) {
                    return this.handleSvsResult(SvsResult.CLIENT_ERROR);
                } else {
                    String _Hebca_SignData = request.getParameter("_Hebca_SignData");
                    if (_Hebca_SignData == null) {
                        return this.handleSvsResult(SvsResult.CLIENT_ERROR);
                    } else {
                        SvsResult result = this.helper.verifyCertSign(-1, 0, _Hebca_Original_Text.getBytes(), _Hebca_Original_Text.getBytes().length, _Hebca_SignCert, _Hebca_SignData, 1, new THostInfoSt());
                        return this.handleSvsResult(result);
                    }
                }
            }
        } else {
            return this.handleSvsResult(SvsResult.CLIENT_ERROR);
        }
    }

    public int verifyCert(String cert) throws Exception {
        SvsResult r = this.helper.verifyCert(cert, 0, 1, (THostInfoSt)null);
        return this.handleSvsResult(r);
    }

    public int verifyCert(String cert, Date time) throws Exception {
        SvsResult r = this.helper.verifyCertAtTime(cert, time, (THostInfoSt)null);
        return this.handleSvsResult(r);
    }

    public int verifySign(String source, String cert, String signature) throws Exception {
        byte[] s = source.getBytes("GBK");
        SvsResult r = this.helper.verifySign(-1, 0, s, s.length, cert, signature, (THostInfoSt)null);
        return this.handleSvsResult(r);
    }

    public int verifyCertSign(String source, String cert, String signature) throws Exception {
        byte[] s = source.getBytes("GBK");
        SvsResult r = this.helper.verifyCertSign(-1, 0, s, s.length, cert, signature, 1, (THostInfoSt)null);
        return this.handleSvsResult(r);
    }

    public String getCertSn(String cert) throws Exception {
        CertParse cp = new CertParse(new Cert(cert), "BC2");
        return cp.getSerialNumberDecString();
    }

    public String getCertSubjectUniqueId(String cert) throws Exception {
        CertParse cp = new CertParse(new Cert(cert), "BC2");
        String cn = cp.getSubject("CN");
        String g = cp.getSubject("G");
        return cn.length() > g.length() ? cn : g;
    }

    public String getCertSubjectDN(String cert) throws Exception {
        CertParse cp = new CertParse(new Cert(cert), "BC2");
        return cp.getSubject();
    }

    public String getRandom() {
        return this.r.nextStr();
    }

    public String getSubjectItem(String cert, int id) throws Exception {
        CertParse cp = new CertParse(new Cert(cert), "BC2");
        return cp.getSubject(id);
    }

    public String getInsure(String cert) throws Exception {
        CertParse cp = new CertParse(new Cert(cert), "BC2");
        String ret = cp.getIssuer("CN");
        if (ret.equalsIgnoreCase("hebca")) {
            return ret;
        } else {
            ret = cp.getIssuer("O");
            if (ret.equalsIgnoreCase("hebca")) {
                return ret;
            } else {
                ret = cp.getIssuer("OU");
                return ret.equalsIgnoreCase("hebca") ? ret : cp.getIssuer("CN");
            }
        }
    }

    public String getCert(String sn) throws Exception {
        GetCertSt r = this.helper.getUserCert(this.caName, sn, (THostInfoSt)null);
        if (r.isError()) {
            throw new Exception(r.getMessage());
        } else {
            return r.getCert();
        }
    }

    public int setCertAppInfo(String szCertID, Map appInfo) throws Exception {
        SvsResult r = this.helper.setCertAppInfo(szCertID, appInfo, (THostInfoSt)null);
        return this.handleSvsResult(r);
    }

    public String getCertInfo(String cert, int type, String itemName) throws Exception {
        GetCertInfoSt r = this.helper.getCertInfo(cert, type, itemName, (THostInfoSt)null);
        if (this.isExceptionMode()) {
            if (r.isError()) {
                throw new SvsException(r.getResult(), r.getMessage());
            } else {
                return r.getCertInfo();
            }
        } else {
            return r.getCertInfo();
        }
    }

    public Map getCertInfoes(String cert, List itemNames) throws Exception {
        GetCertInfoesSt r = this.helper.getCertInfoes(cert, itemNames, (THostInfoSt)null);
        if (this.isExceptionMode()) {
            if (r.isError()) {
                throw new SvsException(r.getResult(), r.getMessage());
            } else {
                return r.getCertInfoes();
            }
        } else {
            return r.getCertInfoes();
        }
    }

    public GetThirdInfoSt getThirdInfo(String keyName, String pkcs7Data, boolean verifyCert) throws Exception {
        GetThirdInfoSt r = this.helper.VerifyThirdInfo(keyName, pkcs7Data, verifyCert, (THostInfoSt)null);
        if (this.isExceptionMode()) {
            if (r.isError()) {
                throw new SvsException(r.getResult(), r.getMessage());
            } else {
                return r;
            }
        } else {
            return r;
        }
    }

    public P7SignAndEnvelopSt P7SignAndEnvelop(byte[] orign, String keyName, byte[] keyValue, String b64Cert) throws Exception {
        P7SignAndEnvelopSt r = this.helper.P7SignAndEnvelop(orign, keyName, keyValue, b64Cert, (THostInfoSt)null);
        if (this.isExceptionMode()) {
            if (r.isError()) {
                throw new SvsException(r.getResult(), r.getMessage());
            } else {
                return r;
            }
        } else {
            return r;
        }
    }

    public static void main(String[] argc) throws Exception {
        //测试用 管火志
        String signCert = "MIIEJjCCAw6gAwIBAgIIcxIA/gAHqQYwDQYJKoZIhvcNAQEFBQAwaTELMAkGA1UEBhMCQ04xDjAMBgNVBAgMBUhlYmVpMRUwEwYDVQQHDAxTaGlqaWF6aHVhbmcxDjAMBgNVBAoMBWhlYmNhMQ4wDAYDVQQLDAVoZWJjYTETMBEGA1UEAwwKSGVCZWlSU0FDQTAeFw0xNDAxMTUwNTM2MDJaFw0xNzAxMTQxNTU5NTlaMIG6MQswCQYDVQQGEwJDTjEPMA0GA1UECAwG5rKz5YyXMRIwEAYDVQQHDAnnn7PlrrbluoQxDjAMBgNVBAoMBWhlYmNhMQ4wDAYDVQQLDAVoZWJjYTEKMAgGA1UECwwBMjEYMBYGA1UEKgwP566h54Gr5b+XMTY3MjI0MRswGQYDVQQBDBI0MjExMjYxOTgzMTIzMTAwMzgxDzANBggqgRyG70oBAwwBMjESMBAGA1UEAwwJ566h54Gr5b+XMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDP5CH4Lgd11NQEuoUYTboZbUwPhbsNS0UkwgWQzAK0ZDxyMuj7MsxFKUzsr7xvoHD8943mUorfJ9Y/Wx2k3x0r+XuWa9RaQRtVIc0ZKx3n9D00yr3Ctqqiz5fXrZlleopTJhpQsxhOjGXEXCHkC0eUUd6XY75ReMgiSwJdxMUpTQIDAQABo4IBAjCB/zAMBgNVHRMEBTADAQEAMBMGA1UdJQQMMAoGCCsGAQUFBwMEMAsGA1UdDwQEAwIAODAfBgNVHSMEGDAWgBS36HwLV2aA9lGeUCcH9wBPvW1+ZzBABgNVHR8EOTA3MDWgM6Axhi9odHRwOi8vY3JsLmhlYmNhLmNvbS9jcmxkb3dubG9hZC9IZUJlaVJTQUNBLmNybDBLBggrBgEFBQcBAQQ/MD0wOwYIKwYBBQUHMAKGL2h0dHA6Ly9jcmwuaGViY2EuY29tL2NybGRvd25sb2FkL0hlQmVpUlNBQ0EuY2VyMB0GA1UdDgQWBBRT/09YoqReAtHdCi1a66A20E1uQDANBgkqhkiG9w0BAQUFAAOCAQEA0tMnmc6WeNlS/JYo6A3r5pQ2RGjuvC7DywgzDXeNrR7S7Kh+5rzzCySr9G+bzej70nM3Wv9IXXjWfmxOPWOOy7O+8clTiqNlfmsw4cXvA5vHE43APdWxam/DWFyuloXfJjHpttDU/iJgW71gomQsG1RXHefdl7XzPWZNelanVo6tX6xXE7GAvwj7dCphfZbcHXyPoLIsK8a4kCk1eWmRYO//iqKoW/tXCDZsliAf/SnZ9W5CIXaLICusSZRQtjfA0eNGnK/Gb5PVBee5X7vTWV96aZc397oZ7nheJkvQIWeHQVTjSUyX30gn6B4pFtY9W6lqafQ4iQMK1FiFjNNjNA==";
        //王一同1
        //String signCert = "MIIDOTCCAt6gAwIBAgIIdB8AQwAzS4IwDAYIKoEcz1UBg3UFADBmMQswCQYDVQQGEwJDTjEOMAwGA1UECAwFaGViZWkxFTATBgNVBAcMDHNoaWppYXpodWFuZzEOMAwGA1UECgwFaGViY2ExDjAMBgNVBAsMBWhlYmNhMRAwDgYDVQQDDAdIQlNNMkNBMB4XDTE4MTIwNTE2MDAwMFoXDTE5MTIwNjE1NTk1OVowgb0xCzAJBgNVBAYTAkNOMRIwEAYDVQQIDAnmsrPljJfnnIExDjAMBgNVBAoMBWhlYmNhMQ4wDAYDVQQLDAVoZWJjYTERMA8GA1UECwwIMTY3ODQ2NDExGTAXBgNVBCoMEOeOi+S4gOWQjDE1MjM4MDQxFjAUBggqgRyG70oBAwwIMTY3ODQ2NDExHzAdBggqgRyG70oBCgwROTExMDEwNk1BMDA1REFCM0YxEzARBgNVBAMMCueOi+S4gOWQjDEwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAARvrsyoXy3w3YYt9gapkxULPED/w5HWuzg3dT5n9FtM3GvX74g8njr4pWOArg7cCj7q3N1PthDTCbWKSX40gHuVo4IBGjCCARYwDAYDVR0TBAUwAwEBADAdBgNVHSUEFjAUBggrBgEFBQcDAgYIKwYBBQUHAwQwCwYDVR0PBAQDAgDAMBEGCWCGSAGG+EIBAQQEAwIAgDAfBgNVHSMEGDAWgBR6ML2l4fY1wq1dD2bVdVm6PoWVHTA9BgNVHR8ENjA0MDKgMKAuhixodHRwOi8vY3JsLmhlYmNhLmNvbS9jcmxkb3dubG9hZC9IQlNNMkNBLmNybDBIBggrBgEFBQcBAQQ8MDowOAYIKwYBBQUHMAKGLGh0dHA6Ly9jcmwuaGViY2EuY29tL2NybGRvd25sb2FkL0hCU00yQ0EuY2VyMB0GA1UdDgQWBBR1NVfc7Fucv8lgSQNGXFuFZCBWlzAMBggqgRzPVQGDdQUAA0cAMEQCIA4zjQeHP5F6onCf67ifC+GyQ63zc9PtLev0IS9VGFSnAiB8fybWKREOf5X2/Uom0oGFkSV9jGvUfBOEpMXsk+o5IA==";
        //测试电子保函
        //String signCert = "MIIDQjCCAuagAwIBAgIIdB8AVwApNl8wDAYIKoEcz1UBg3UFADBmMQswCQYDVQQGEwJDTjEOMAwGA1UECAwFaGViZWkxFTATBgNVBAcMDHNoaWppYXpodWFuZzEOMAwGA1UECgwFaGViY2ExDjAMBgNVBAsMBWhlYmNhMRAwDgYDVQQDDAdIQlNNMkNBMB4XDTE4MDIyNTE2MDAwMFoXDTIxMDIyNjE1NTk1OVowgcUxCzAJBgNVBAYTAkNOMRIwEAYDVQQIDAnmsrPljJfnnIExDjAMBgNVBAoMBWhlYmNhMQ4wDAYDVQQLDAVoZWJjYTERMA8GA1UECwwIMTY3ODQ2NDExITAfBgNVBCoMGOa1i+ivleeUteWtkOS/neWHvTc1NjExMzEWMBQGCCqBHIbvSgEDDAgxNjc4NDY0MTEXMBUGCCqBHIbvSgEKDAkwMDAwMDAwMDAxGzAZBgNVBAMMEua1i+ivleeUteWtkOS/neWHvTBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABDaSF9oeoapYWPb9nUVWhz5JziUCQKdo87IM3ixUB9NkIxdu6DNXvKG7khee4jkGYOuQRigQ/KWhq4mTTtURPd6jggEaMIIBFjAMBgNVHRMEBTADAQEAMB0GA1UdJQQWMBQGCCsGAQUFBwMCBggrBgEFBQcDBDALBgNVHQ8EBAMCAMAwEQYJYIZIAYb4QgEBBAQDAgCAMB8GA1UdIwQYMBaAFHowvaXh9jXCrV0PZtV1Wbo+hZUdMD0GA1UdHwQ2MDQwMqAwoC6GLGh0dHA6Ly9jcmwuaGViY2EuY29tL2NybGRvd25sb2FkL0hCU00yQ0EuY3JsMEgGCCsGAQUFBwEBBDwwOjA4BggrBgEFBQcwAoYsaHR0cDovL2NybC5oZWJjYS5jb20vY3JsZG93bmxvYWQvSEJTTTJDQS5jZXIwHQYDVR0OBBYEFAn0lAyASfszqjWryRA5MnVdPQXEMAwGCCqBHM9VAYN1BQADSAAwRQIhAOpigFSHp13U8P2nD9RQcJoCxT84Wl7MfbiLjV40Bc7AAiAIXJcKgBa6JuXV8+svR7pgcc+qBbrrRC3W21DsLtU1CA==";
        try {
            System.out.println(ResourceUtils.getURL("classpath:").getPath());
            init(ResourceUtils.getURL("classpath:").getPath() + "HebcaSvs.properties");
            byte[] orign = "www.hebca.com".getBytes();
            byte[] keyvalue = MyBase64.encode("123456".getBytes(), 6);

            // "MTIzNDU2Nzg="(12345678)
            //P7SignAndEnvelopSt enveloped = getInstance().P7SignAndEnvelop(orign, "hebcaSoftEngine.rsaKey", "MTIzNDU2Nzg=".getBytes(), signCert);

            //System.out.println("加密结果：" + enveloped.getEnvelopedData());
            System.out.println("本地解析-获取证书唯一项：" + getInstance().getCertSubjectUniqueId(signCert));
            System.out.println("本地解析-单位名称：" + getInstance().getSubjectItem(signCert, 0));
            System.out.println("本地解析-单位组织结构代码：" + getInstance().getSubjectItem(signCert, 8));
            System.out.println("本地解析-主题字符串：" + getInstance().getCertSubjectDN(signCert));
            System.out.println();
            System.out.println();
            List names = new ArrayList();
            names.add("Subject.USERID");
            names.add("Subject.ALIAS");
            names.add("Subject.USER");
            names.add("Subject.DN");
            Map ret = getInstance().getCertInfoes(signCert, names);
            System.out.println("SVS批量解析-获取证书唯一项：" + ret.get("Subject.USERID"));
            System.out.println("SVS批量解析-获取单位名称：" + ret.get("Subject.USER"));
            System.out.println("SVS批量解析-获取单位组织结构代码：" + ret.get("Subject.ALIAS"));
            System.out.println("SVS批量解析-获取主题字符串：" + ret.get("Subject.DN"));
            System.out.println();
            if (getInstance().getCertSubjectDN(signCert).equals(getInstance().getCertInfo(signCert, 0, "DN")) && getInstance().getCertSubjectDN(signCert).equals(ret.get("Subject.DN"))) {
                System.out.println("DN相等");
            } else {
                System.out.println("DN不相等");
            }
        } catch (Exception var7) {
            System.out.println(var7.getMessage());
        }

//        CertParse cp=new CertParse(new Cert(signCert), BouncyCastleProvider.PROVIDER_NAME);
//        System.out.println("CN is " + HebcaVerify.getInstance().getSubjectItem(signCert, 7));
//        System.out.println("certSn is" + HebcaVerify.getInstance().getCertSn(signCert));
//        System.out.println("本地解析-获取证书唯一项：" + HebcaVerify.getInstance().getCertSubjectUniqueId(signCert));
//        System.out.println("本地解析-单位名称：" + HebcaVerify.getInstance().getSubjectItem(signCert, 0));
//        System.out.println("本地解析-单位组织结构代码：" + HebcaVerify.getInstance().getSubjectItem(signCert, 8));
//        System.out.println("本地解析-主题字符串：" + HebcaVerify.getInstance().getCertSubjectDN(signCert));
//        System.out.println("1" + HebcaVerify.getInstance().getSubjectItem(signCert, 1));
//        System.out.println("2" + HebcaVerify.getInstance().getSubjectItem(signCert, 2));
//        System.out.println("3" + HebcaVerify.getInstance().getSubjectItem(signCert, 3));
//        System.out.println("4" + HebcaVerify.getInstance().getSubjectItem(signCert, 4));
//        System.out.println("5" + HebcaVerify.getInstance().getSubjectItem(signCert, 5));
//        System.out.println("6" + HebcaVerify.getInstance().getSubjectItem(signCert, 6));
//        System.out.println("7" + HebcaVerify.getInstance().getSubjectItem(signCert, 7));
//        System.out.println("8" + HebcaVerify.getInstance().getSubjectItem(signCert, 8));
//        System.out.println("cp " + cp.getSubject(CertParse.DN_CN));
//        System.out.println("cp " + cp.getSubject(CertParse.DN_GIVENNAME));
//        System.out.println("cp " + cp.getSubject(CertParse.DN_PHONE));
//        System.out.println("cp " + cp.getSubject(CertParse.DN_STATE));
//        System.out.println("cp " + cp.getSerialNumber());

    }
}