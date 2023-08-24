package com.dusizhong.examples.pay.tonglian;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.pay.util.HttpConnectionUtil;
import com.dusizhong.examples.pay.util.SybUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * 通联老收银宝支付通道
 */
@Service
public class TonglianOldService {

    private final static Logger logger = LoggerFactory.getLogger(TonglianService.class);

    //支付网关
    public static String HOSTNAME;

    @Value("${tonglian.hostname}")
    public void setHOSTNAME(String hostname) {
        this.HOSTNAME = hostname;
    }

    //二维码支付
    public static String QR_CODE;

    @Value("${tonglian.qr_code}")
    public void setQR_CODE(String qr_code) {
        this.QR_CODE = qr_code;
    }

    //支付结果通知地址
    public static String NOTIFY_URL;

    @Value("${tonglian.notify_url}")
    public void setNotifyUrl(String notifyUrl) {
        this.NOTIFY_URL = notifyUrl;
    }

    //通联退款网关
    public static String REFUND;

    @Value("${tonglian.refund}")
    public void setREFUND(String refund) {
        this.REFUND = refund;
    }

    @Value("${tonglian.qr_code.refund}")
    String QR_REFUND;

    //商户号
    public static String CUSID;

    @Value("${tonglian.cusid}")
    public void setCUSID(String cusid) {
        this.CUSID = cusid;
    }

    //应用ID
    public static String APPID;

    @Value("${tonglian.appid}")
    public void setAPPID(String appid) {
        this.APPID = appid;
    }

    public static String APPKEY;

    @Value("${tonglian.appkey}")
    public void setAPPKEY(String appkey) {
        this.APPKEY = appkey;
    }

    public static String VALID_TIME;

    @Value("${tonglian.valid_time}")
    public void setValidTime(String validTime) {
        this.VALID_TIME = validTime;
    }

    @Value("${tonglian.ret_url_weixin}")
    String ret_url_weixin;

    /**
     * 网关支付
     *
     * @param outTradeNo
     * @param centTotalFee
     * @param random
     * @param bidSectionName
     * @return
     */
    public Map<String, String> tonglianpay(String outTradeNo, String centTotalFee, String random, String bidSectionName, String paytype) {
        TreeMap<String, String> reqData = new TreeMap<String, String>();
        reqData.put("cusid", CUSID);
        reqData.put("appid", APPID);
        reqData.put("charset", "UTF-8");
        reqData.put("notifyurl", NOTIFY_URL);
        reqData.put("trxamt", centTotalFee);
        reqData.put("orderid", outTradeNo);
        reqData.put("goodsinf", "投标保函服务费");
        reqData.put("randomstr", random);
        reqData.put("paytype", paytype);//B2C,B2B
        reqData.put("sign", sign(reqData, APPKEY));
        logger.info("网关支付发起支付请求：" + reqData);
        return reqData;
    }

    /**
     * 支付宝二维码支付
     *
     * @param centTotalFee
     * @param outTradeNo
     * @param random
     * @return
     */
    public String qr_codepay(String centTotalFee, String outTradeNo, String random, String body) {
        String url = "";
        HttpConnectionUtil http = new HttpConnectionUtil(QR_CODE);
        try {
            http.init();
            TreeMap<String, String> params = new TreeMap<String, String>();
            params.put("cusid", CUSID);
            params.put("appid", APPID);
            params.put("trxamt", centTotalFee);
            params.put("reqsn", outTradeNo);
            params.put("paytype", "A01");
            params.put("randomstr", random);
            params.put("notify_url", NOTIFY_URL);
            params.put("body", body);
            params.put("sign", SybUtil.sign(params, APPKEY));
            byte[] bys = http.postParams(params, true);
            String result = new String(bys, "UTF-8");
            logger.info("支付宝二维码发起支付请求：" + result);
            Map<String, String> map = handleRes(result);
            if (map.get("retcode").equals("SUCCESS")) {
                if (map.get("cusid").equals(CUSID)) {
                    url = map.get("payinfo");
                    logger.info("支付宝二维码发起支付请求成功");
                } else logger.info("商户号错误");
            } else logger.info("支付宝二维码支付返回码错误");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 微信支付
     *
     * @param centTotalFee
     * @param outTradeNo
     * @param random
     * @return
     */
    public String tonglianweixin(String centTotalFee, String outTradeNo, String body, String random) throws Exception {
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("cusid", CUSID);
        params.put("appid", APPID);
        params.put("version", "12");
        params.put("trxamt", centTotalFee);
        params.put("reqsn", outTradeNo);
        params.put("charset", "UTF-8");
        params.put("returl", ret_url_weixin);
        params.put("notify_url", NOTIFY_URL);
        params.put("body", body);
        params.put("randomstr", random);
        params.put("sign", SybUtil.sign(params, APPKEY));
        logger.info("微信支付请求：" + params);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
        }
        logger.info("微信支付链接：" + "https://syb.allinpay.com/apiweb/h5unionpay/unionorder?" + sb.substring(0, sb.length() - 1));
        return "https://syb.allinpay.com/apiweb/h5unionpay/unionorder?" + sb.substring(0, sb.length() - 1);
    }

    /**
     * 网关退款
     *
     * @param outTradeNo
     * @param centTotalFee
     * @param random
     * @return
     */
    @Transactional
    public String refund(String outTradeNo, String centTotalFee, String random) {
        String code = "fail";
        HttpConnectionUtil http = new HttpConnectionUtil(REFUND);
        try {
            http.init();
            TreeMap<String, String> params = new TreeMap<String, String>();
            params.put("cusid", CUSID);
            params.put("appid", APPID);
            params.put("reqsn", System.currentTimeMillis() + "");
            params.put("trxamt", centTotalFee);
            params.put("orderid", outTradeNo);
            params.put("randomstr", random);
            params.put("sign", sign(params, APPKEY));
            byte[] bys = http.postParams(params, true);
            String result = new String(bys, "UTF-8");
            logger.info("通联网关退款结果:" + result);
            Map<String, String> map = handleResult(result);
            if (map.get("retcode").equals("SUCCESS")) {
                if (map.get("trxstatus").equals("0000")) {
                    //获取交易记录
//                    TransRecord transRecord = transRecordRepo.findByOutTradeNo(map.get("orderid"));
//                    Guarantee guarantee = guaranteeRepo.findBySerialNo(map.get("orderid"));
//                    //更新交易记录
//                    transRecord.setStatus("REFUND");
//                    transRecord.setNotifyTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                    transRecordRepo.save(transRecord);
//                    //更新保函记录
//                    guarantee.setStatus("REJECT");
//                    guarantee.setRefundTransactionId(map.get("trxid"));
//                    guarantee.setRefund(true);
//                    guarantee.setReApply(true);
//                    guarantee.setPaid("已退款");
//                    guarantee.setStatusName("已退回");
//                    guarantee.setRefundTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                    guaranteeRepo.save(guarantee);
                    logger.info("通联退款成功" + map.get("orderid"));
                    code = "success";
                } else logger.info("通联退款交易状态码不正确" + map.get("orderid"));
            } else logger.info("通联退款返回码不正确" + map.get("orderid"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 支付宝二维码退款
     *
     * @param centTotalFee
     * @param outTradeNo
     * @param random
     * @return
     */
    public String qrcodeRefund(String centTotalFee, String outTradeNo, String random) {
        String code = "fail";
        HttpConnectionUtil http = new HttpConnectionUtil(QR_REFUND);
        try {
            http.init();
            TreeMap<String, String> params = new TreeMap<String, String>();
            params.put("cusid", CUSID);
            params.put("appid", APPID);
            params.put("trxamt", centTotalFee);
            params.put("reqsn", outTradeNo);
            params.put("oldreqsn", outTradeNo);
            params.put("randomstr", random);
            params.put("sign", SybUtil.sign(params, APPKEY));
            byte[] bys = http.postParams(params, true);
            String result = new String(bys, "UTF-8");
            logger.info("支付宝二维码退款结果:" + result);
            Map<String, String> map = handleResult(result);
            if (map.get("retcode").equals("SUCCESS")) {
                if (map.get("trxstatus").equals("0000")) {
                    //获取交易记录
//                    TransRecord transRecord = transRecordRepo.findByOutTradeNo(map.get("reqsn"));
//                    Guarantee guarantee = guaranteeRepo.findBySerialNo(map.get("reqsn"));
//                    //更新交易记录
//                    transRecord.setStatus("REFUND");
//                    transRecord.setNotifyTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                    transRecordRepo.save(transRecord);
//                    //更新保函记录
//                    guarantee.setStatus("REJECT");
//                    guarantee.setRefundTransactionId(map.get("trxid"));
//                    guarantee.setRefund(true);
//                    guarantee.setReApply(true);
//                    guarantee.setPaid("已退款");
//                    guarantee.setStatusName("已退回");
//                    guarantee.setRefundTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                    guaranteeRepo.save(guarantee);
                    logger.info("支付宝二维码退款成功" + map.get("reqsn"));
                    code = "success";
                } else logger.info("支付宝二维码退款交易状态码不正确" + map.get("reqsn"));
            } else logger.info("支付宝二维码退款返回码不正确" + map.get("reqsn"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 通联微信退款
     *
     * @param centTotalFee
     * @param outTradeNo
     * @param random
     * @return
     */
    public String weixinrefund(String centTotalFee, String outTradeNo, String random) {
        String code = "fail";
        HttpConnectionUtil http = new HttpConnectionUtil(QR_REFUND);
        try {
            http.init();
            TreeMap<String, String> params = new TreeMap<String, String>();
            params.put("cusid", CUSID);
            params.put("appid", APPID);
            params.put("version", "12");
            params.put("trxamt", centTotalFee);
            params.put("reqsn", outTradeNo);
            params.put("oldreqsn", outTradeNo);
            params.put("randomstr", random);
            params.put("sign", SybUtil.sign(params, APPKEY));
            byte[] bys = http.postParams(params, true);
            String result = new String(bys, "UTF-8");
            logger.info("微信二维码退款结果:" + result);
            Map<String, String> map = handleResult(result);
            if (map.get("retcode").equals("SUCCESS")) {
                if (map.get("trxstatus").equals("0000")) {
                    //获取交易记录
//                    TransRecord transRecord = transRecordRepo.findByOutTradeNo(map.get("reqsn"));
//                    Guarantee guarantee = guaranteeRepo.findBySerialNo(map.get("reqsn"));
//                    //更新交易记录
//                    transRecord.setStatus("REFUND");
//                    transRecord.setNotifyTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                    transRecordRepo.save(transRecord);
//                    //更新保函记录
//                    guarantee.setStatus("REJECT");
//                    guarantee.setRefundTransactionId(map.get("trxid"));
//                    guarantee.setRefund(true);
//                    guarantee.setReApply(true);
//                    guarantee.setPaid("已退款");
//                    guarantee.setStatusName("已退回");
//                    guarantee.setRefundTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                    guaranteeRepo.save(guarantee);
                    logger.info("微信退款成功" + map.get("reqsn"));
                    code = "success";
                } else logger.info("微信退款交易状态码不正确" + map.get("reqsn"));
            } else logger.info("微信退款返回码不正确" + map.get("reqsn"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public static String sign(TreeMap<String, String> params, String key) {
        params.put("key", key);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length() > 0) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        System.out.println("明文:" + sb.toString());
        String sign = md5(sb.toString()).toUpperCase();
        System.out.println("密文:" + sign);
        params.remove("key");
        return sign;
    }

    public static String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(data.getBytes("utf-8"));
            byte[] hash = md.digest();
            StringBuffer outStrBuf = new StringBuffer(32);
            for (int i = 0; i < hash.length; i++) {
                int v = hash[i] & 0xFF;
                if (v < 16) {
                    outStrBuf.append('0');
                }
                outStrBuf.append(Integer.toString(v, 16).toLowerCase());
            }
            return outStrBuf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static <T> T json2Obj(String jsonstr, Class<T> cls) {
        T obj = (T) JSONObject.parseObject(jsonstr, cls);
        return obj;
    }

    private static Map<String, String> handleResult(String result) throws Exception {
        Map map = json2Obj(result, Map.class);
        if (map == null) {
            throw new Exception("返回数据错误");
        }
        if ("SUCCESS".equals(map.get("retcode"))) {
            TreeMap tmap = new TreeMap();
            tmap.putAll(map);
            String sign = tmap.remove("sign").toString();
            String sign1 = sign(tmap, APPKEY);
            if (sign1.equals(sign)) {
                return map;
            } else {
                throw new Exception("验证签名失败");
            }
        } else {
            throw new Exception(map.get("retmsg").toString());
        }
    }

    public static Map<String, String> handleRes(String result) throws Exception {
        Map map = SybUtil.json2Obj(result, Map.class);
        if (map == null) {
            throw new Exception("返回数据错误");
        }
        if ("SUCCESS".equals(map.get("retcode"))) {
            TreeMap tmap = new TreeMap();
            tmap.putAll(map);
            String sign = tmap.remove("sign").toString();
            String sign1 = SybUtil.sign(tmap, APPKEY);
            if (sign1.toLowerCase().equals(sign.toLowerCase())) {
                return map;
            } else {
                throw new Exception("验证签名失败");
            }

        } else {
            throw new Exception(map.get("retmsg").toString());
        }
    }

}
