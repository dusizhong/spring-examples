package com.dusizhong.examples.pay.hebpay;

import com.dusizhong.examples.pay.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 河北银行聚合支付服务
 */
@Service
public class HebPayService {

    private final static Logger logger = LoggerFactory.getLogger(HebPayService.class);

    //支付网关
    public static String GATEWAY_URL;
    @Value("${hebpay.gateway.url}")
    public void setHebpayGateWayUrl(String gateWayUrl) { this.GATEWAY_URL = gateWayUrl; }
    //支付结果通知地址
    public static String NOTIFY_URL;
    @Value("${hebpay.notify.url}")
    public void setHebpayNotifyUrl(String notifyUrl) { this.NOTIFY_URL = notifyUrl; }
    //商户服务器ip
    public static String MCH_IP;
    @Value("${hebpay.mch.ip}")
    public void setHebpayMchIp(String mchIp) { this.MCH_IP = mchIp; }

    //微信支付参数
    public static String WEIXIN_SERVICE;
    @Value("${weixin.service}")
    public void setWeixinService(String service) { this.WEIXIN_SERVICE = service; }
    public static String WEIXIN_MCH_ID;
    @Value("${weixin.mch.id}")
    public void setWeixinMchId(String mchId) { this.WEIXIN_MCH_ID = mchId; }
    public static String WEIXIN_MCH_KEY;
    @Value("${weixin.mch.key}")
    public void setWeixinMchKey(String mchKey) { this.WEIXIN_MCH_KEY = mchKey; }

    //支付宝支付参数
    public static String ALIPAY_SERVICE;
    @Value("${alipay.service}")
    public void setAlipayService(String service) { this.ALIPAY_SERVICE = service; }
    public static String ALIPAY_MCH_ID;
    @Value("${alipay.mch.id}")
    public void setAlipayMchId(String mchId) { this.ALIPAY_MCH_ID = mchId; }
    public static String ALIPAY_MCH_KEY;
    @Value("${alipay.mch.key}")
    public void setAlipayMchKey(String mchKey) { this.ALIPAY_MCH_KEY = mchKey; }

    //银联支付参数
    public static String UNIONPAY_SERVICE;
    @Value("${unionpay.service}")
    public void setUnionpayService(String service) { this.UNIONPAY_SERVICE = service; }
    public static String UNIONPAY_MCH_ID;
    @Value("${unionpay.mch.id}")
    public void setUnionpayMchId(String mchId) { this.UNIONPAY_MCH_ID = mchId; }
    public static String UNIONPAY_MCH_KEY;
    @Value("${unionpay.mch.key}")
    public void setUnionpayMchKey(String mchKey) { this.UNIONPAY_MCH_KEY = mchKey; }

    /**
     * 发起支付请求，返回二维码
     * @param out_trade_no
     * @param total_fee
     * @param body
     * @return
     */
    public String unipay(String out_trade_no, String total_fee, String body, String SERVICE, String MCH_ID, String MCH_KEY) {

        String qrUrl = "";
        //拼装签名数据
        String nonce_str = HebPayUtils.generateNonceStr();
        Map<String, String> reqData = new HashMap<>();
        reqData.put("service", SERVICE);
        reqData.put("mch_id", MCH_ID);
        reqData.put("out_trade_no", out_trade_no);
        reqData.put("body", body);
        reqData.put("total_fee", total_fee);
        reqData.put("mch_create_ip", MCH_IP);
        reqData.put("notify_url", NOTIFY_URL);
        reqData.put("nonce_str", nonce_str);
        //生成签名
        try {
            reqData.put("sign", HebPayUtils.generateSignature(reqData, MCH_KEY));
            String xmlReqData = XmlUtils.mapToXml(reqData);
            //发送支付请求
            logger.info("发起支付请求：" + xmlReqData);
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
            headers.setContentType(type);
            HttpEntity<String> formEntity = new HttpEntity<String>(xmlReqData, headers);
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.postForObject(GATEWAY_URL, formEntity, String.class);
            //获取返回结果
//            result = new String(result.getBytes("ISO-8859-1"), "utf-8");
            logger.info("发起支付请求结果：" + result);
            Map<String, String> resultData = XmlUtils.xmlToMap(result);
            if(resultData.get("status").equals("0")) {
                if(resultData.get("result_code").equals("0")) {
                    if(HebPayUtils.isSignatureValid(result, MCH_KEY)) {
                        if (resultData.get("mch_id").equals(MCH_ID) && resultData.get("nonce_str").equals(nonce_str)) {
                            logger.info("发起支付请求成功");
                            qrUrl = resultData.get("code_img_url");
                        } else logger.info("商户号或随机码错误");
                    } else logger.info("验签失败");
                } else logger.info("发起支付请求失败" + resultData.get("err_code"));
            } else logger.info("通信失败" + resultData.get("message"));
        } catch (Exception e) {
            logger.info("生成签名失败！" + e.getMessage());
        }
        return qrUrl;
    }
}
