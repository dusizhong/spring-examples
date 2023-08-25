package com.dusizhong.examples.pay.tonglian.h5;

import java.util.Map;
import java.util.TreeMap;

public class PayUtil {

    /**
     * 签名
     * @param params
     * @return
     * @throws Exception
     */
    public static String sign(Map<String,String> params, String appkey) throws Exception {
        if(params.containsKey("sign")) params.remove("sign"); //签名明文组装不包含sign字段
        params.put("key", appkey);
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry : params.entrySet()) {
            if(entry.getValue()!=null&&entry.getValue().length()>0) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        if(sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        String sign = Utils.md5(sb.toString().getBytes("UTF-8"));//记得是md5编码的加签
        params.remove("key");
        return sign;
    }

    public static boolean validSign(TreeMap<String,String> param,String appkey) throws Exception{
        if(param!=null&&!param.isEmpty()){
            if(!param.containsKey("sign"))
                return false;
            String sign = param.get("sign").toString();
            String mysign = sign(param, appkey);
            return sign.toLowerCase().equals(mysign.toLowerCase());
        }
        return false;
    }
}