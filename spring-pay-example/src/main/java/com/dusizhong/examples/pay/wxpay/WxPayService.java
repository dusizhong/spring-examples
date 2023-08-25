package com.dusizhong.examples.pay.wxpay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;


@Service
public class WxPayService {

    private final static Logger logger = LoggerFactory.getLogger(WxPayService.class);

    //微信支付网关参数
    public static String APP_ID;
    @Value("${wxpay.app.id}")
    public void setWxpayAppId(String appId) { this.APP_ID = appId; }
    public static String MCH_ID;
    @Value("${wxpay.mch.id}")
    public void setWxpayMchId(String mchId) { this.MCH_ID = mchId; }
    public static String MCH_KEY;
    @Value("${wxpay.mch.key}")
    public void setWxpayMchKey(String mchKey) { this.MCH_KEY = mchKey; }
    public static String GATEWAY_URL;
    @Value("${wxpay.gateway.url}")
    public void setWxpayGateWayUrl(String gateWayUrl) { this.GATEWAY_URL = gateWayUrl; }
    //支付结果通知地址
    public static String NOTIFY_URL;
    @Value("${wxpay.notify.url}")
    public void setWxpayNotifyUrl(String notifyUrl) { this.NOTIFY_URL = notifyUrl; }

    /**
     * 发起支付请求，返回二维码
     * @param out_trade_no
     * @param total_fee(单位分)
     * @param body
     * @return
     */
    public String unipay(String out_trade_no, String total_fee, String body) {

        String qrUrl = "";
        //拼装签名数据
        String nonce_str = generateNonceStr();
        Map<String, String> reqData = new HashMap<>();
        reqData.put("appid", APP_ID);
        reqData.put("mch_id", MCH_ID);
        reqData.put("nonce_str", nonce_str);
        reqData.put("body", body);
        reqData.put("out_trade_no", out_trade_no);
        reqData.put("total_fee", total_fee);
        reqData.put("spbill_create_ip", "127.0.0.1");
        reqData.put("trade_type", "NATIVE");
        reqData.put("notify_url", NOTIFY_URL);

        //生成签名
        try {
            reqData.put("sign", generateSignature(reqData, MCH_KEY));
            String xmlReqData = XmlUtils.mapToXml(reqData);
            //发送支付请求
            logger.info("发起微信支付请求：" + xmlReqData);
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
            headers.setContentType(type);
            HttpEntity<String> formEntity = new HttpEntity<String>(xmlReqData, headers);
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.postForObject(GATEWAY_URL + "/pay/unifiedorder", formEntity, String.class);
            //获取返回结果
            result = new String(result.getBytes("ISO-8859-1"), "utf-8");
            logger.info("发起微信支付请求结果：" + result);
            Map<String, String> resultData = XmlUtils.xmlToMap(result);
            if(resultData.get("return_code").equals("SUCCESS")) {
                if(resultData.get("result_code").equals("SUCCESS")) {
                    if(isSignatureValid(result, MCH_KEY)) {
                        if(resultData.get("mch_id").equals(MCH_ID) && resultData.get("appid").equals(APP_ID)) {
                            logger.info("发起微信支付请求成功");
                            qrUrl = resultData.get("code_url");
                        } else logger.info("APPID或商户号不符");
                    } else logger.info("验签失败");
                } else logger.info("发起支付请求失败" + resultData.get("err_code"));
            } else logger.info("通信失败" + resultData.get("return_msg"));
        } catch (Exception e) {
            logger.info("生成签名失败！" + e.getMessage());
        }
        return qrUrl;
    }


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
        Map<String, String> data = XmlUtils.xmlToMap(xmlStr);
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
        java.security.MessageDigest md = MessageDigest.getInstance("MD5");
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
