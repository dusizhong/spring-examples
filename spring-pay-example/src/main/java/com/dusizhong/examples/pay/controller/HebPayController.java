package com.dusizhong.examples.pay.controller;

import com.dusizhong.examples.pay.entity.PayOrder;
import com.dusizhong.examples.pay.enums.OrderStatusEnum;
import com.dusizhong.examples.pay.enums.PayTypeEnum;
import com.dusizhong.examples.pay.hebpay.HebPayService;
import com.dusizhong.examples.pay.hebpay.HebPayUtils;
import com.dusizhong.examples.pay.model.BaseResp;
import com.dusizhong.examples.pay.repository.PayOrderRepository;
import com.dusizhong.examples.pay.util.*;
import com.dusizhong.examples.pay.wxpay.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/hebpay")
public class HebPayController {

    private final static Logger logger = LoggerFactory.getLogger(HebPayController.class);

    @Autowired
    private HebPayService hebPayService;
    @Autowired
    private PayOrderRepository payOrderRepository;

    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    public BaseResp pay(@RequestBody PayOrder post, HttpServletRequest request) {
        if(StringUtils.isEmpty(post.getOrderType())) return BaseResp.error("订单类型不能为空");
        if(StringUtils.isEmpty(post.getPayType())) return BaseResp.error("支付方式不能为空");
        if(StringUtils.isEmpty(post.getPayAmount())) return BaseResp.error("支付金额不能为空");
        if(!Calculator.isAmount(post.getPayAmount())) return BaseResp.error("支付金额无效");
        String centTotalFee = Calculator.convertToCent(post.getPayAmount());//金额转换为分
        if(StringUtils.isEmpty(centTotalFee)) return BaseResp.error("金额转换失败");
        String body = "测试业务支付"; //body<128字符
        if(body.length() > 42) body = body.substring(0, 42);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String random = RandomStringUtils.random(10, false, true);
        String orderNo = "100" + timestamp + random; //订单号（30位）
        post.setId(SqlUtils.createId());
        post.setOrderNo(orderNo);
        post.setPayerId("7");
        post.setPayerCode("123");
        post.setPayerName("付款人");
        post.setPayerIp(IpUtils.getIpAddr(request));
        post.setReceiverId("xxx");
        post.setReceiverCode("xxx");
        post.setReceiverName("xxx");
        post.setPayFee("0");
        post.setPayTime(SqlUtils.getDateTime());
        post.setFrontUrl("");
        post.setBackUrl("http://192.168.1.101/hebpay/notify");
        post.setStatus(OrderStatusEnum.PENDING.getCode());
        post.setChannelName("HEBPAY");
        post.setMemo(body);
        if(PayTypeEnum.WX.getCode().equals(post.getPayType())) {
            String wxQrUrl = hebPayService.unipay(post.getOrderNo(), centTotalFee, body, HebPayService.WEIXIN_SERVICE, HebPayService.WEIXIN_MCH_ID, HebPayService.WEIXIN_MCH_KEY);
            if(StringUtils.isEmpty(wxQrUrl)) return BaseResp.error("发起微信支付请求失败");
            else post.setPayQrCode(wxQrUrl);
        }
        if(PayTypeEnum.ZFB.getCode().equals(post.getPayType())) {
            String aliQrUrl = hebPayService.unipay(post.getOrderNo(), centTotalFee, body, HebPayService.ALIPAY_SERVICE, HebPayService.ALIPAY_MCH_ID, HebPayService.ALIPAY_MCH_KEY);
            if (StringUtils.isEmpty(aliQrUrl)) return BaseResp.error("发起支付宝支付请求失败");
            else post.setPayQrCode(aliQrUrl);
        }
        if(PayTypeEnum.YL.getCode().equals(post.getPayType())) {
            String unionQrUrl = hebPayService.unipay(post.getOrderNo(), centTotalFee, body, HebPayService.UNIONPAY_SERVICE, HebPayService.UNIONPAY_MCH_ID, HebPayService.UNIONPAY_MCH_KEY);
            if (StringUtils.isEmpty(unionQrUrl)) return BaseResp.error("发起银联支付请求失败");
            else post.setPayQrCode(unionQrUrl);
        } else {
            //线下支付
        }
        payOrderRepository.save(post);
        return BaseResp.success(post);
    }

    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public String notify(HttpServletRequest request, HttpServletResponse response) {
        String result = "fail";
        String inputLine;
        String notityXml = "";
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        //获取返回的数据
        try {
            request.setCharacterEncoding("UTF-8");
            while ((inputLine = request.getReader().readLine()) != null) {
                notityXml += inputLine;
            }
            if (!StringUtils.isEmpty(notityXml)) {
                logger.info("notify支付结果: " + notityXml);
                Map<String, String> map = XmlUtils.xmlToMap(notityXml);
                if(map.get("status").equals("0")) {
                    if (map.get("result_code").equals("0")) {
                        //验签
                        String mch_id = "";
                        String mch_key = "";
                        String trade_type = map.get("trade_type");
                        if(trade_type.endsWith(HebPayService.WEIXIN_SERVICE)) {
                            mch_id = HebPayService.WEIXIN_MCH_ID;
                            mch_key = HebPayService.WEIXIN_MCH_KEY;
                        }
                        else if(trade_type.endsWith(HebPayService.ALIPAY_SERVICE)) {
                            mch_id = HebPayService.ALIPAY_MCH_ID;
                            mch_key = HebPayService.ALIPAY_MCH_KEY;
                        }
                        else if(trade_type.endsWith(HebPayService.UNIONPAY_SERVICE)) {
                            mch_id = HebPayService.UNIONPAY_MCH_ID;
                            mch_key = HebPayService.UNIONPAY_MCH_KEY;
                        }
                        if(HebPayUtils.isSignatureValid(notityXml,mch_key )) {
                            if (map.get("mch_id").equals(mch_id)) {
                                String ori_out_trade_no = map.get("out_trade_no");
                                String out_trade_no = map.get("out_trade_no");
                                if(out_trade_no.length() == 32) out_trade_no = out_trade_no.substring(12);
                                //获取交易记录
                                PayOrder payOrder = payOrderRepository.findByOrderNo(out_trade_no);
                                if(payOrder != null) {
                                    if(payOrder.getStatus().equals("SUCCESS")) { //处理notify重复发送
                                        result = "success";
                                    } else {
                                        payOrder.setChannelMchId(mch_id);
                                        payOrder.setChannelPayerId(map.get("openid"));
                                        payOrder.setChannelPayerId(map.get("transaction_id"));
                                        payOrder.setStatus("SUCCESS");
                                        payOrder.setChannelPayTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                                        payOrderRepository.save(payOrder);
                                        //更新业务
                                        logger.info("notify支付成功");
                                        result = "success";
                                    }
                                } else logger.info("notify获取交易记录失败");
                            } else logger.info("notify商户号不符");
                        } else logger.info("notify验签失败");
                    } else logger.info("notify支付失败");
                } else logger.info("notify通信失败");
            } else logger.info("notify获取返回数据失败");
            request.getReader().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
