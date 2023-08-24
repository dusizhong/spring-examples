package com.dusizhong.examples.pay.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.pay.entity.PayOrder;
import com.dusizhong.examples.pay.enums.OrderStatusEnum;
import com.dusizhong.examples.pay.enums.PayTypeEnum;
import com.dusizhong.examples.pay.model.BaseResp;
import com.dusizhong.examples.pay.repository.PayOrderRepository;
import com.dusizhong.examples.pay.tonglian.TonglianService;
import com.dusizhong.examples.pay.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@RestController
@RequestMapping("/tonglian")
public class TonglianController {

    @Autowired
    private TonglianService tonglianService;
    @Autowired
    private PayOrderRepository payOrderRepository;

    //todo: 生成系统建议增加订单控制，同一笔业务避免重复发起多条生成订单
    @PostMapping("/pay")
    public BaseResp pay(@RequestBody PayOrder post, HttpServletRequest request) {
        if(StringUtils.isEmpty(post.getOrderType())) return BaseResp.error("订单类型不能为空");
        if(StringUtils.isEmpty(post.getPayType())) return BaseResp.error("支付方式不能为空");
        if(StringUtils.isEmpty(post.getPayAmount())) return BaseResp.error("支付金额不能为空");
        if(!Calculator.isAmount(post.getPayAmount())) return BaseResp.error("支付金额无效");
        //LocalDateTime expiredTime = LocalDateTime.parse(post.getExpiredTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //if(LocalDateTime.now().isAfter(expiredTime)) return BaseResp.error("订单已过期，不能支付");
        //if("SUCCESS".equals(post.getStatus())) return BaseResp.error("订单已支付，无须重复支付");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String random = RandomStringUtils.random(10, false, true);
        String orderNo = "100" + timestamp + random; //订单号（30位）
        post.setId(SqlUtils.createId());
        post.setOrderNo(orderNo);
        post.setPayerId("7");
        post.setPayerCode("123");
        post.setPayerName("付款人");
        post.setPayerIp(IpUtils.getIpAddr(request));
        post.setReceiverId("c723e6cf-bb22-466f-9918-9b0bb8f6caae");
        post.setReceiverCode("911300006010731179");
        post.setReceiverName("河北省成套招标有限公司");
        post.setPayFee("0");
        post.setPayTime(SqlUtils.getDateTime());
        post.setFrontUrl("");
        post.setBackUrl("http://192.168.1.101/PayOrder/notify");
        post.setStatus(OrderStatusEnum.PENDING.getCode());
        //发起支付
        post.setChannelName("TONGLIAN");
        if(PayTypeEnum.WX.getCode().equals(post.getPayType()) || PayTypeEnum.ZFB.getCode().equals(post.getPayType()) || PayTypeEnum.YL.getCode().equals(post.getPayType())) {
            BaseResp result = tonglianService.consumeApply(post);
            if(!result.getCode().equals(200)) return BaseResp.error(result.getMsg());
            JSONObject jsonObject = JSON.parseObject(result.getData().toString());
            post.setPayQrCode(QrCodeUtils.generateQrCode(jsonObject.getString("payInfo")));
        }
        if(PayTypeEnum.B2B.getCode().equals(post.getPayType()) || PayTypeEnum.B2C.getCode().equals(post.getPayType())) {
            BaseResp result = tonglianService.consumeApply(post);
            if(!result.getCode().equals(200)) return BaseResp.error(result.getMsg());
            result = tonglianService.payBySMS(post);
            if(!result.getCode().equals(200)) return BaseResp.error(result.getMsg());
            post.setPayUrl(result.getData().toString());
        } else {
            //线下支付
        }
        payOrderRepository.save(post);
        return BaseResp.success(post);
    }

    @RequestMapping("/notify")
    public String notify(HttpServletRequest request) throws UnsupportedEncodingException {
        String result = "fail";
        request.setCharacterEncoding("utf-8");
        BaseResp resp = tonglianService.notify(request);
        if(resp.getCode().equals(200)) {
            JSONObject respData = (JSONObject) JSONObject.toJSON(resp.getData());
            if (respData.get("status").equals("OK")) {
                //更新交易记录
                PayOrder payOrder = payOrderRepository.findByOrderNo(respData.getString("bizOrderNo"));
                if(!ObjectUtils.isEmpty(payOrder)) {
                    if (payOrder.getStatus().equals(OrderStatusEnum.SUCCESS.getCode())) { //处理notify重复发送
                        result = "success";
                    } else {
                        payOrder.setChannelMchId(respData.getString("cusid"));
                        payOrder.setChannelPayTime(respData.getString("payDatetime"));
                        payOrder.setChannelPayerId(respData.getString("acct"));
                        payOrder.setChannelTransId(respData.getString("chnltrxid"));
                        payOrder.setStatus(OrderStatusEnum.SUCCESS.getCode());
                        payOrderRepository.save(payOrder);
                        //更新业务
                    }
                } else log.info("通联支付回调：获取订单记录失败，无此订单号" + respData.getString("bizOrderNo"));
            } else {
                log.info("通联支付：支付失败");
            }
        }
        return result;
    }


    //确认支付（回调未成功，可主动发起查询确认支付结果）
    @Transactional
    @PostMapping("/confirm")
    public BaseResp confirm(@RequestBody PayOrder post) {
        if(StringUtils.isEmpty(post.getId())) return BaseResp.error("id不能为空");
        PayOrder payOrder = payOrderRepository.findOne(post.getId());
        if(payOrder == null) return BaseResp.error("id无效");
        //检查通道支付状态
        JSONObject params = new JSONObject();
        params.put("bizOrderNo", payOrder.getOrderNo());
        BaseResp resp = tonglianService.getOrderDetail(params);
        if(!resp.getCode().equals(200)) return BaseResp.error(resp.getMsg());
        JSONObject jsonObject = JSON.parseObject(resp.getData().toString());
        if("4".equals(jsonObject.getString("orderStatus"))) { //4支付成功
            post.setChannelMchId(jsonObject.getString("cusid"));
            post.setChannelTransId(jsonObject.getString("chnltrxid"));
            post.setChannelPayerId(jsonObject.getString("acct"));
            post.setChannelPayTime(jsonObject.getString("payDatetime"));
            post.setStatus(OrderStatusEnum.SUCCESS.getCode());
            payOrderRepository.save(post);
            //更新业务状态

        }
        return BaseResp.success();
    }
}
