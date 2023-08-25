package com.dusizhong.examples.pay.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.dusizhong.examples.pay.alipay.AliPayService;
import com.dusizhong.examples.pay.model.BaseResp;
import com.dusizhong.examples.pay.util.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付宝支付
 */
@RestController
@RequestMapping("/alipay")
public class AlipayController {

    private final static Logger logger = LoggerFactory.getLogger(AlipayController.class);

    @Autowired
    private AliPayService aliPayService;

    @RequestMapping(value = "/pay")
    public BaseResp pay() {
        String outTradeNo = RandomStringUtils.randomNumeric(20);
        String result = aliPayService.pay(outTradeNo, "1", "支付宝", "支付宝支付测试");
        if(StringUtils.isEmpty(result)) return BaseResp.error("发起支付请求失败");
        return BaseResp.success(result);
    }

    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public String notifyPay(HttpServletRequest request) throws Exception {
        //获取支付宝异步通知参数
        Map<String,String> params = getParamsMap(request);
        logger.info("notify: " + params);
        //签名验证
        boolean signVerified = AlipaySignature.rsaCheckV1(params, AliPayService.alipay_public_key, AliPayService.charset, AliPayService.sign_type);
        //验签成功，执行商户操作
        if(signVerified) {
            logger.info("notify: 验签成功");
            if(params.get("app_id").equals(AliPayService.app_id)) {
                String out_trade_no = params.get("out_trade_no");
                    //TRADE_FINISHED:通知触发条件是商户签约的产品不支持退款功能的前提下，买家付款成功
                    //或者，商户签约的产品支持退款功能的前提下，交易已经成功并且已经超过可退款期限。
                    //TRADE_SUCCESS:通知触发条件是商户签约的产品支持退款功能的前提下，买家付款成功
                    if (params.get("trade_status").equals("TRADE_SUCCESS") || params.get("trade_status").equals("TRADE_FINISHED")) {
                        logger.info("notify: 交易成功 " + out_trade_no);
                    } else {
                        logger.warn("notify: 交易失败");
                        // 如果返回不是支付成功，将进行订单查询支付结果查询，当结果为支付成功时，重新执行商户操作
                        // orderService.tradeQuery(out_trade_no, ClientUtil.getClientIP(request), user);
                    }
            } else logger.warn("notify: appId错误");
        } else {
            logger.warn("alipay notify: 验签失败");
        }
        return "success";
    }

    /**
     * 支付宝跳转地址
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public void returnPay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=" + AliPayService.charset);
        if(!StringUtils.isEmpty(request.getQueryString())) {
            //获取支付宝异步通知参数
            Map<String, String> params = getParamsMap(request);
            logger.info("return" + params);
            //签名验证
            boolean signVerified = AlipaySignature.rsaCheckV1(params, AliPayService.alipay_public_key, AliPayService.charset, AliPayService.sign_type);
            //验签成功，执行商户操作
            if(signVerified) {
                if(params.get("app_id").equals(AliPayService.app_id)) {
                    String out_trade_no = params.get("out_trade_no");
//                    TransRecord transRecord = transRecordRepo.findByOutTradeNo(out_trade_no);
//                    if(transRecord != null) {
//                        response.getWriter().write("支付成功");
//                    } else response.getWriter().write("支付失败");
                } else {
                    response.getWriter().write("支付失败");
                }
            } else {
                response.getWriter().println("验签失败");
            }
        }
    }

    // 解析支付宝返回参数
    private Map<String, String> getParamsMap(HttpServletRequest request) {
        Map<String,String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            try {
//                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
            params.put(name, valueStr);
        }
        return params;
    }
}
