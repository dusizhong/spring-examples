package com.dusizhong.examples.pay.controller;

import com.dusizhong.examples.pay.model.BaseResp;
import com.dusizhong.examples.pay.util.Calculator;
import com.dusizhong.examples.pay.util.RandomStringUtils;
import com.dusizhong.examples.pay.wxpay.WxPayService;
import com.dusizhong.examples.pay.wxpay.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.dusizhong.examples.pay.wxpay.WxPayService.*;

/**
 * 微信支付
 */
@RestController
@RequestMapping("/wx")
public class WxpayController {

    private final static Logger logger = LoggerFactory.getLogger(WxpayController.class);

    @Autowired
    private WxPayService wxPayService;

    @RequestMapping(value = "/pay")
    public BaseResp pay() {
        String result = "";
        String centTotalFee = Calculator.convertToCent("0.01");//金额转换为分
        String outTradeNo = RandomStringUtils.randomNumeric(20);
        //处理body超长
        String body = "微信支付测试"; //body<128字符
        if(body.length() > 42) body = body.substring(0, 42);
        result = wxPayService.unipay(outTradeNo, centTotalFee, body);
        if(StringUtils.isEmpty(result)) return BaseResp.error("发起支付请求失败");
        return BaseResp.success(result);
    }

    @Transactional
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public String notifyPay(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String result = "";
        String inputLine;
        String notityXml = "";
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        //获取微信给返回的数据
        try {
            while ((inputLine = request.getReader().readLine()) != null) {
                notityXml += inputLine;
            }
            if (!StringUtils.isEmpty(notityXml)) {
                logger.info("微信notify: " + notityXml);
                Map<String, String> resultData = XmlUtils.xmlToMap(notityXml);
                if(resultData.get("return_code").equals("SUCCESS")) {
                    if(resultData.get("result_code").equals("SUCCESS")) {
                        if(isSignatureValid(notityXml, MCH_KEY)) {
                            logger.info("微信notify: 验签成功" + resultData.get("out_trade_no"));
                            if(resultData.get("mch_id").equals(MCH_ID) && resultData.get("appid").equals(APP_ID)) {
                                String out_trade_no = resultData.get("out_trade_no");
                                logger.info("微信notify: 交易成功" + out_trade_no);
                                            // 拼装应答数据给微信
                                            Map<String, String> resData = new HashMap<>();
                                            resData.put("return_code", "SUCCESS");
                                            resData.put("return_msg", "OK");
                                            result = XmlUtils.mapToXml(resData);
                            } else logger.info("微信notify: APPID或商户号不符");
                        } else logger.info("微信notify: 验签失败");
                    } else logger.info("微信notify: " + resultData.get("err_code"));
                } else logger.info("微信notify: 通信失败" + resultData.get("return_msg"));
            } else logger.info("微信notify: 获取返回结果为空");
            request.getReader().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
