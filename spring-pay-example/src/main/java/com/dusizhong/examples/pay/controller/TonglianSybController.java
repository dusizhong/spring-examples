package com.dusizhong.examples.pay.controller;

import com.dusizhong.examples.pay.entity.PayOrder;
import com.dusizhong.examples.pay.enums.PayTypeEnum;
import com.dusizhong.examples.pay.model.BaseResp;
import com.dusizhong.examples.pay.tonglian.syb.TonglianSybService;
import com.dusizhong.examples.pay.util.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 通联收银宝支付
 */
@RestController
@RequestMapping("/tonglian/syb")
public class TonglianSybController {

    @Autowired
    private TonglianSybService tonglianSybService;

    private final static Logger logger = LoggerFactory.getLogger(TonglianSybController.class);

    /**
     * 网关支付
     * 个人网银B2B、企业网银
     *
     * 需要前端拼接跳转：（以下参考）
     * https://open.allinpay.com/gateway?charset=utf-8&method=allinpay.yunst.orderService.payBySMS&appAuthToken=&appId=1522844930580008962&bizContent=%7B%22bizUserId%22%3A%229df8e411-e623-44ba-8530-9e761fe9ada5%22%2C%22bizOrderNo%22%3A%22101202308241503261760000000001%22%2C%22tradeNo%22%3A%22%22%2C%22consumerIp%22%3A%22127.0.0.1%22%2C%22verificationCode%22%3A%22%22%7D&format=JSON&sign=D6Q7nDQFLNBplF2XJrLPl3n7OgtMymhhQP%2BgX3RsDiy7SAkarBLVawc1qIwufkltoFQSFHsjOgluRpD9j5b3HU3F2cxsAD7Dghfn2o%2B86%2BLp6SL3Y9dTLlhfmkKhooaWIA4P3TkmL6p0vyJy6etqV%2FRItTAHiLMiO9DF9bs%2BLVrRwtAvO%2Fc3E45H6ZxheGiiPVt%2F0b78H1JPGa1iMbnhmFte8ztO2Xvfct116gp4tfyGbr3jlE%2BuGeX8E8UpbeD%2B%2FgIbedqBE5OfQJQiqViihRpD7zWoJPeFA1LDyC7r6GS%2BQKpcKU3vgfredZlTPLsA%2FgFGvWK5myxE3a9tcldPzw%3D%3D&notifyUrl=&signType=SHA256WithRSA&version=1.0&timestamp=2023-08-24+15%3A03%3A26
     */
    @RequestMapping("/pay")
    public BaseResp pay(@RequestBody PayOrder post) {
        String outTradeNo = RandomStringUtils.randomNumeric(20);
        String biz = "测试收银宝网关支付";//商品描述信息
        String random = RandomStringUtils.randomNumeric(10);
        Map<String, String> result = tonglianSybService.tonglianpay(outTradeNo, "1", random, PayTypeEnum.B2C.getCode());
        return BaseResp.success(result.toString());
    }

    /**
     * 支付宝二维码支付
     */
    @RequestMapping("/alipay")
    public BaseResp alipay() throws Exception {
        String outTradeNo = RandomStringUtils.randomNumeric(20);
        String random = RandomStringUtils.randomNumeric(10);
        String url = tonglianSybService.qr_codepay("1", outTradeNo, random, "测试支付宝支付");
        if (StringUtils.isEmpty(url)) logger.info("发起支付宝支付请求失败");
        return BaseResp.success(url);
    }

    /**
     * 微信二维码支付
     */
    @RequestMapping("/wxpay")
    public BaseResp wxpay() throws Exception {
        String outTradeNo = RandomStringUtils.randomNumeric(20);
        String random = RandomStringUtils.randomNumeric(10);
        String url = tonglianSybService.tonglianweixin("1", outTradeNo, "测试微信支付", random);
        if (StringUtils.isEmpty(url)) logger.info("发起微信支付请求失败");
        return BaseResp.success(url);
    }

    @RequestMapping("/notify")
    public String notify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String result = "fail";
        request.setCharacterEncoding("utf-8");
        TreeMap<String, String> params = getParams(request);
        logger.info("支付回调结果:" + params);
        try {
            boolean isSign = validSign(params, TonglianSybService.APPKEY);// 接受到推送通知,首先验签
            logger.info("支付回调验签结果:" + isSign + params.get("cusorderid"));
            if (isSign) {
                if (params.get("trxstatus").equals("0000")) {
                    logger.info("notify支付成功" + params.get("cusorderid"));
                    result = "success";
                } else logger.info("交易结果码不正确" + params.get("cusorderid"));
            } else logger.info("notify验签不通过" + params.get("cusorderid"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean validSign(TreeMap<String, String> param, String appkey) {
        if (param != null && !param.isEmpty()) {
            if (!param.containsKey("sign")) {
                return false;
            }
            param.put("key", appkey);//将分配的appkey加入排序
            StringBuilder sb = new StringBuilder();
            String sign = param.get("sign").toString();
            param.remove("sign");
            for (String key : param.keySet()) {
                String value = param.get(key);
                sb.append(key).append("=").append(value).append("&");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            String blank = sb.toString();
            System.out.println(blank + ";" + sign);
            return sign.toLowerCase().equals(TonglianSybService.md5(blank));
        }
        return false;
    }

    /**
     * 动态遍历获取所有收到的参数,此步非常关键,因为收银宝以后可能会加字段,动态获取可以兼容由于收银宝加字段而引起的签名异常
     * @param request
     * @return
     */
    private TreeMap<String, String> getParams(HttpServletRequest request){
        TreeMap<String, String> map = new TreeMap<String, String>();
        Map reqMap = request.getParameterMap();
        for(Object key:reqMap.keySet()){
            String value = ((String[])reqMap.get(key))[0];
            System.out.println(key+";"+value);
            map.put(key.toString(),value);
        }
        return map;
    }
}
