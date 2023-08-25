package com.dusizhong.examples.pay.tonglian.h5;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class H5PayService {

    private static String GATEWAY_URL = "https://syb.allinpay.com/apiweb/h5unionpay/unionorder";
    private static String APP_ID = "xxx";
    private static String MCH_ID = "xxx";
    public static String MCH_KEY = "xxx";
    private static String RETURN_URL = "http://kj48z9.natappfree.cc";
    private static String NOTIFY_URL = "http://kj48z9.natappfree.cc/notify";

    /**
     * 发起H5支付
     * @param tradeNo 商户订单号
     * @param trxamt 金额(分)
     * @param body 商品信息
     * @return
     * @throws Exception
     */
    public String pay(String tradeNo, long trxamt, String body) throws Exception {
        String result;
//		HttpUtil http = new HttpUtil(GATEWAY_URL + "/pay");
//		http.init();
        StringBuilder sb = new StringBuilder();
        Map<String,String> params = new HashMap<>();
        params.put("appid", APP_ID); //appid
        params.put("cusid", MCH_ID); //商户号
        params.put("version", "12"); //默认版本号
        params.put("trxamt", String.valueOf(trxamt)); //金额，单位分
        params.put("reqsn", tradeNo); //商户订单号
        params.put("charset", "utf-8");
        params.put("returl", RETURN_URL);
        params.put("notify_url", NOTIFY_URL);
        params.put("body", body); //订单标题
        params.put("randomstr", Utils.getRandom(8)); //随机数
        params.put("sign", PayUtil.sign(params, MCH_KEY));
        //发起H5支付
        System.out.println("发起H5支付：");
        for(Map.Entry<String,String> entry : params.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(),"UTF-8")).append("&");
        }
//		byte[] bys = http.postParams(params, true);
//		result = new String(bys,"UTF-8");
//		Map<String,String> map = handleResult(result);
//		return map;

        result = GATEWAY_URL + "?" + sb.substring(0,sb.length()-1);
        System.out.println(result);
        return result;
    }


    /**
     * 交易撤销
     * 只能撤销当天的交易，全额退款，实时返回退款结果
     * @param tradeNo 撤销订单号
     * @param trxamt 金额（分）
     * @param oldTrxid 原交易流水号
     * @return
     * @throws Exception
     */
    public String cancel(String tradeNo, long trxamt, String oldTrxid) throws Exception {
        Map<String,String> params = new HashMap<>();
        params.put("cusid", MCH_ID);
        params.put("appid", APP_ID);
        params.put("trxamt", String.valueOf(trxamt));
        params.put("reqsn", tradeNo);
        params.put("oldtrxid", oldTrxid);
        params.put("randomstr", Utils.getRandom(8));
        params.put("sign", PayUtil.sign(params, MCH_KEY));
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
        headers.setContentType(type);
//        HttpEntity<String> formEntity = new HttpEntity<String>(params, headers);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.postForObject(GATEWAY_URL + "/cancel", params, String.class);
        //获取返回结果
        result = new String(result.getBytes("ISO-8859-1"), "utf-8");
        System.out.println("发起交易撤销结果：" + result);


        Map<String,String> map = handleResult(result);
        return result;
    }

    public Map<String,String> refund(long trxamt,String reqsn,String oldtrxid,String oldreqsn) throws Exception{
        HttpUtil http = new HttpUtil(GATEWAY_URL + "/refund");
        http.init();
        TreeMap<String,String> params = new TreeMap<String,String>();
        params.put("cusid", MCH_ID);
        params.put("appid", APP_ID);
        params.put("version", "11");
        params.put("trxamt", String.valueOf(trxamt));
        params.put("reqsn", reqsn);
        params.put("oldreqsn", oldreqsn);
        params.put("oldtrxid", oldtrxid);
        params.put("randomstr", Utils.getRandom(8));
        params.put("sign", PayUtil.sign(params, MCH_KEY));
        byte[] bys = http.postParams(params, true);
        String result = new String(bys,"UTF-8");
        Map<String,String> map = handleResult(result);
        return map;
    }

    public Map<String,String> query(String reqsn,String trxid) throws Exception{
        HttpUtil http = new HttpUtil(GATEWAY_URL + "/query");
        http.init();
        TreeMap<String,String> params = new TreeMap<String,String>();
        params.put("cusid", MCH_ID);
        params.put("appid", APP_ID);
        params.put("version", "11");
        params.put("reqsn", reqsn);
        params.put("trxid", trxid);
        params.put("randomstr", Utils.getRandom(8));
        params.put("sign", PayUtil.sign(params, MCH_KEY));
        byte[] bys = http.postParams(params, true);
        String result = new String(bys,"UTF-8");
        Map<String,String> map = handleResult(result);
        return map;
    }


    public static Map<String,String> handleResult(String result) throws Exception{
        Map map = Utils.json2Obj(result, Map.class);
        if(map == null){
            throw new Exception("返回数据错误");
        }
        if("SUCCESS".equals(map.get("retcode"))){
            TreeMap tmap = new TreeMap();
            tmap.putAll(map);
            String sign = tmap.remove("sign").toString();
            String sign1 = PayUtil.sign(tmap, MCH_KEY);
            if(sign1.toLowerCase().equals(sign.toLowerCase())){
                return map;
            }else{
                throw new Exception("验证签名失败");
            }

        }else{
            throw new Exception(map.get("retmsg").toString());
        }
    }
}
