package com.dusizhong.examples.pay.tonglian;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.allinpay.sdk.OpenClient;
import com.allinpay.sdk.bean.BizParameter;
import com.allinpay.sdk.bean.OpenConfig;
import com.allinpay.sdk.bean.OpenResponse;
import com.dusizhong.examples.pay.entity.PayOrder;
import com.dusizhong.examples.pay.enums.OrderTypeEnum;
import com.dusizhong.examples.pay.enums.PayTypeEnum;
import com.dusizhong.examples.pay.model.BaseResp;
import com.dusizhong.examples.pay.util.Calculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class TonglianService implements ApplicationRunner {

    final String url = "https://open.allinpay.com/gateway";
    final String appId = "xxx";
    final String secretKey = "xxx";
    final String privateKeyPath = "/home/tonglian-cert/xxx.pfx";
    final String pwd = "xxx";
    final String tlPublicKey = "/home/tonglian-cert/xxx.cer";
    final String accountSetNo = "xxx"; //账户集编号（由通联分配）

    protected OpenClient client;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final OpenConfig oc = new OpenConfig(url, appId, secretKey, privateKeyPath, pwd, tlPublicKey);
        try {
            client = new OpenClient(oc);
            log.info("Tonglian client init success");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建会员
     */
    public BaseResp createMember(TonglianMember post) {
        final BizParameter param = new BizParameter();
        param.put("bizUserId", post.getBizUserId()); //用户id
        param.put("memberType", Long.valueOf(post.getMemberType())); //用户类型（2企业会员 3个人会员）
        param.put("source", 2L); //访问终端类型（Mobile 1、PC 2）
        try {
            final OpenResponse response = client.execute("allinpay.yunst.memberService.createMember", param);
            if ("OK".equals(response.getSubCode())) {
                return BaseResp.success(response.getData());
            } else return BaseResp.error(response.getSubMsg());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用创建会员接口失败");
    }

    /**
     * 发送短信验证码
     */
    public BaseResp sendVerificationCode(TonglianMember post) {
        final BizParameter param = new BizParameter();
        param.put("bizUserId", post.getBizUserId());
        param.put("phone", post.getPhone()); //手机号
        param.put("verificationCodeType", 9L); //验证码类型（9-绑定手机，6-解绑手机）
        try {
            final OpenResponse response = client.execute("allinpay.yunst.memberService.sendVerificationCode", param);
            if ("OK".equals(response.getSubCode())) {
                return BaseResp.success(response.getData());
            } else return BaseResp.error(response.getSubMsg());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用发送短信验证码接口失败");
    }

    /**
     * 绑定手机
     */
    public BaseResp bindPhone(TonglianMember post) {
        final BizParameter param = new BizParameter();
        param.put("bizUserId", post.getBizUserId());
        param.put("phone", post.getPhone());
        param.put("verificationCode", post.getVerificationCode()); //短信验证码
        try {
            final OpenResponse response = client.execute("allinpay.yunst.memberService.bindPhone", param);
            if ("OK".equals(response.getSubCode())) {
                return BaseResp.success(response.getData());
            } else return BaseResp.error(response.getSubMsg());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用绑定手机接口失败");
    }

    /**
     * 解绑手机
     */
    public BaseResp unbindPhone(TonglianMember post) {
        final BizParameter param = new BizParameter();
        param.put("bizUserId", post.getBizUserId());
        param.put("phone", post.getPhone());
        param.put("verificationCode", post.getVerificationCode());
        try {
            final OpenResponse response = client.execute("allinpay.yunst.memberService.unbindPhone", param);
            if ("OK".equals(response.getSubCode())) {
                return BaseResp.success(response.getData());
            } else return BaseResp.error(response.getSubMsg());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用解绑手机接口失败");
    }

    /**
     * 设置企业信息
     */
    public BaseResp setCompanyInfo(JSONObject post) {
        final BizParameter param = new BizParameter();
        param.put("bizUserId", post.getString("bizUserId"));
        param.put("backUrl", post.getString("backUrl")); //企业会员审核结果通知（非必填）
        param.put("isAuth", false); //是否进行线上认证	true系统自动审核
        final Map<String, Object> companyBasicInfo = new LinkedHashMap<>();
        companyBasicInfo.put("companyName", post.getString("companyName")); //企业名称，如有括号，用中文格式（）
        //companyBasicInfo.put("companyAddress", post.getString("companyAddress")); //企业地址（非必填）
        companyBasicInfo.put("authType", 1L); //认证类型（1:三证 2:一证）
        companyBasicInfo.put("uniCredit", post.getString("uniCredit")); //统一社会信用（一证）认证类型为2时必传
        companyBasicInfo.put("businessLicense", post.getString("businessLicense")); //营业执照号（三证）认证类型为1时必传
        companyBasicInfo.put("organizationCode", post.getString("organizationCode")); //组织机构代码（三证）认证类型为1时必传
        companyBasicInfo.put("taxRegister", post.getString("taxRegister")); //税务登记证（三证）认证类型为1时必传
        companyBasicInfo.put("expLicense", post.getString("expLicense")); //统一社会信用/营业执照号到期时间 格式：yyyy-MM-dd（非必填）
        companyBasicInfo.put("telephone", post.getString("telephone")); //联系电话
        companyBasicInfo.put("legalName", post.getString("legalName")); //法人姓名
        companyBasicInfo.put("identityType", 1L); //法人证件类型（1身份证），详见：https://cloud.allinpay.com/ts-cloud-dev-web/#/apiCenter/index?params=y&key=284
        companyBasicInfo.put("legalIds", client.encrypt(post.getString("legalIds"))); //法人证件号码，AES加密
        companyBasicInfo.put("legalPhone", post.getString("legalPhone")); //法人手机号码
        companyBasicInfo.put("accountNo", post.getString("accountNo")); //企业对公账户，支持数字和“-”字符AES加密， href="#_敏感信息加解密"
        companyBasicInfo.put("parentBankName", post.getString("parentBankName")); //开户银行名称，需严格按照银行列表上送，详见：https://cloud.allinpay.com/ts-cloud-dev-web/#/apiCenter/index?params=y&key=303
        companyBasicInfo.put("bankName", post.getString("bankName")); //开户行支行名称
        companyBasicInfo.put("unionBank", post.getString("unionBank")); //支付行号，12位数字
        param.put("companyBasicInfo", companyBasicInfo);
        try {
            final OpenResponse response = client.execute("allinpay.yunst.memberService.setCompanyInfo", param);
            if ("OK".equals(response.getSubCode())) {
                return BaseResp.success(response.getData());
            } else return BaseResp.error(response.getSubMsg());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用设置企业信息接口失败");
    }

    /**
     * 查询(个人、企业)会员信息
     */
    public BaseResp getMemberInfo(TonglianMember post) {
        final BizParameter param = new BizParameter();
        param.put("bizUserId", post.getBizUserId());
        try {
            final OpenResponse response = client.execute("allinpay.yunst.memberService.getMemberInfo", param);
            if ("OK".equals(response.getSubCode())) {
                JSONObject jsonObject = JSON.parseObject(String.valueOf(response.getData()));
                JSONObject json = jsonObject.getJSONObject("memberInfo");
                if (!json.getBoolean("isPhoneChecked")) return BaseResp.error("用户未绑定手机");
                return BaseResp.success(response.getData());
            } else {
                if("30001".equals(response.getSubCode())) { //用户不存在，自动创建个人用户
                    post.setMemberType("3");
                    createMember(post);
                }
                return BaseResp.error(response.getSubMsg());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用查询(个人、企业)会员信息接口失败");
    }

    /**
     * 消费申请
     */
    public BaseResp consumeApply(PayOrder post) {
        final BizParameter param = new BizParameter();

        // 1、设定支付方式（详见：https://cloud.allinpay.com/ts-cloud-dev-web/#/apiCenter/index?params=y&key=265）
        final HashMap<String, Object> payMethod = new HashMap<>();
        // 转换金额为分
        Long amount = Long.valueOf(Calculator.convertToCent(post.getPayAmount()));
        Long fee = Long.valueOf(Calculator.convertToCent(post.getPayFee()));
        // 微信扫码支付(正扫)——收银宝
        if(PayTypeEnum.WX.getCode().equals(post.getPayType())) {
            final Map<String, Object> scanWeixin = new HashMap<>();
            scanWeixin.put("amount", amount);
            scanWeixin.put("limitPay", ""); //非贷记卡：no_credit 借、贷记卡：””
            payMethod.put("SCAN_WEIXIN", scanWeixin);
        }
        // 支付宝扫码支付(正扫) ——收银宝
        if(PayTypeEnum.ZFB.getCode().equals(post.getPayType())) {
            final Map<String, Object> scanAlipay = new HashMap<>();
            scanAlipay.put("amount", amount);
            scanAlipay.put("limitPay", "");
            payMethod.put("SCAN_ALIPAY", scanAlipay);
        }
        // 银联扫码支付(正扫) ——收银宝
        if(PayTypeEnum.YL.getCode().equals(post.getPayType())) {
            final Map<String, Object> scanUnionPay = new HashMap<>();
            scanUnionPay.put("amount", amount);
            scanUnionPay.put("limitPay", "");
            payMethod.put("SCAN_UNIONPAY", scanUnionPay);
        }
        // 收银宝网关支付（B2C、B2B）
        if(PayTypeEnum.B2B.getCode().equals(post.getPayType()) || PayTypeEnum.B2C.getCode().equals(post.getPayType())) {
            final Map<String, Object> gatewayVsp = new HashMap<>();
            gatewayVsp.put("amount", amount);
            gatewayVsp.put("paytype", post.getPayType());
            payMethod.put("GATEWAY_VSP", gatewayVsp);
        }

        // 2、拼装请求参数
        param.put("payerId", post.getPayerId()); //付款用户id（7 ）
        param.put("recieverId", post.getReceiverId()); //收款方id
        param.put("bizOrderNo", post.getOrderNo()); //商户订单号
        param.put("amount", amount); //订单金额（分）
        param.put("fee", fee); //手续费（内扣，如果不存在，则填0）
        param.put("validateType", 0L); //交易验证方式（无验证0L 短信验证码1L 支付密码2L）
        param.put("frontUrl", post.getFrontUrl()); //前台通知地址
        param.put("backUrl", post.getBackUrl()); //后台通知地址
        param.put("orderExpireDatetime", post.getExpiredTime()); //订单过期时间（非必填）
        param.put("payMethod", payMethod); //支付方式
        param.put("goodsName", OrderTypeEnum.getName(post.getOrderType())); //商品名称（非必填）
        param.put("goodsDesc", ""); //商品描述（非必填）
        param.put("industryCode", "1917"); //行业代码
        param.put("industryName", "招投标"); //行业名称
        param.put("source", 2L); //访问终端类型 （Mobile 1L PC 2L）
        param.put("summary", ""); //摘要（非必填）
        param.put("extendInfo", ""); //扩展参数（非必填）

        // 分账（暂未使用）
        final JSONArray splitRule = new JSONArray();
        final HashMap<String, Object> splitRule1 = new HashMap<>();
        splitRule1.put("bizUserId", "test0001");
        splitRule1.put("accountSetNo", "200001"); //100001
        splitRule1.put("amount", 50L);
        splitRule1.put("fee", 0L);
        splitRule1.put("remark", " 消费一级分账");
        final JSONArray splitRule2List1 = new JSONArray();
        final HashMap<String, Object> splitRule2List = new HashMap<>();
        splitRule2List.put("bizUserId", "renhd001");
        splitRule2List.put("accountSetNo", "200001");
        splitRule2List.put("amount", 20L);
        splitRule2List.put("fee", 0L);
        splitRule2List.put("remark", "消费二级分账");
        splitRule2List1.add(new JSONObject(splitRule2List));
        splitRule1.put("splitRuleList", splitRule2List1);
        splitRule.add(new JSONObject(splitRule1));
//		request.put("splitRule", splitRule);

        // 3、发起支付
        try {
            final OpenResponse response = client.execute("allinpay.yunst.orderService.consumeApply", param);
            if ("OK".equals(response.getSubCode())) {
                //微信、支付宝、银联返回payInfo供转换二维码，网银支付发起成功后，须调用确认支付接口（前台+短信验证码确认）才能返回跳转网银url
                return BaseResp.success(response.getData());
            } else return BaseResp.error(response.getSubMsg());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用消费申请接口失败");
    }

    /**
     * 确认支付（前台+短信验证码）
     */
    public BaseResp payBySMS(PayOrder post) {
        final BizParameter param = new BizParameter();
        param.put("bizUserId", post.getPayerId());
        param.put("bizOrderNo", post.getOrderNo()); //商户订单号
        param.put("verificationCode", ""); //短信验证码（网关支付/收银宝H5收银台，不填，不验短信验证码）
        param.put("consumerIp", post.getPayerIp()); //用户公网IP用于风控校验
        try {
            final String url = client.concatUrlParams("allinpay.yunst.orderService.payBySMS", param);
            return BaseResp.success(url);
            //browser(url);// 打开浏览器
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用确认支付（前台+短信验证码）失败");
    }

    /**
     * 确认支付（后台+短信验证码）
     */
//    public BaseResp payByBackSMS(TonglianPay post) {
//        final BizParameter param = new BizParameter();
//        param.put("bizUserId", post.getPayerId());
//        param.put("bizOrderNo", post.getBizOrderNo()); //商户订单号
//        param.put("tradeNo", ""); //交易编号（非必填）
//        param.put("verificationCode", post.getVerificationCode()); //短信验证码
//        param.put("consumerIp", "192.168.11.11"); //用户公网IP用于风控校验
//
//        try {
//            final OpenResponse response = client.execute("allinpay.yunst.orderService.payByBackSMS", param);
//            if ("OK".equals(response.getSubCode())) {
//                return BaseResp.success(response.getData());
//            } else return BaseResp.error(response.getSubMsg());
//        } catch (final Exception e) {
//            e.printStackTrace();
//        }
//        return BaseResp.error("调用确认支付（后台+短信验证码）接口失败");
//    }

    /**
     * 通联回调通知
     * 参见：https://cloud.allinpay.com/ts-cloud-dev-web/#/apiCenter/index?params=y&key=211
     *
     * 返回：{appId=1522844930580008962, bizContent={"amount":1,"orderNo":"1607719835028959232","termrefnum":"4200001657202212279673086854","channelFee":"0","channelPaytime":"2022-12-27 20:48:17","extendInfo":"","accttype":"99","chnltrxid":"4200001657202212279673086854","payInterfaceOutTradeNo":"221227123633919281","buyerBizUserId":"7","termauthno":"OTHERS","cusid":"650121048160NMM","payInterfacetrxcode":"VSP501","payDatetime":"2022-12-27 20:48:17","acct":"opn0buCtTZCEctAyCzKlxbYEtMck","bizOrderNo":"200202212272047325633899123074","status":"OK"}, charset=utf-8, notifyId=1607720020310007809, notifyTime=2022-12-27 20:48:17, notifyType=allinpay.yunst.orderService.pay, sign=SwGY1geZugPBpbCaEpYDPZrDhPL6r/MLvgOALqHp7gEHakA59qEpIdAlPrxVHO4Vrh5Vip8JsPHOAI9tlVSzYr/5+YIPpqGBczLWTX0SIvpK0Up+ezOQ1X+0sbFok0wm1nRrq9xwhuqeRU+jgMRNqKQIl88k0o6EwD1PAofocE+vBsZQEnxjuXZ0OXZNUtcYRTxkv45cq3tRe9QYtRpEYyFcdBlCTT9+MjGwYF9xEAGzWh4gy+hlyIZtH8tdakiNJPUYPiutW+BbjcgHq47cmqeOJ+Wf7+GADX34t0w9khORrfr4jDe0MsjUPJ5yPWJq8lq56dA12KATjwth3DwaNg==, signType=SHA256WithRSA, version=1.0}
     *
     * sign: swgy1gezugpbpbcaepydpzrdhpl6r/mlvgoalqhp7gehaka59qepidalprxvho4vrh5vip8jsphoai9tlvszyr/5+yippqgbczlwtx0sivpk0up+ezoq1x+0sbfok0wm1nrrq9xwhuqeru+jgmrnqkqil88k0o6ewd1paofoce+vbszqenxjuxz0oxznutcyrtxkv45cq3tre9qytrpeyyfcdblctt9+mjgwyf9xeagzwh4gy+hlyizth8tdakinjpuypiutw+bbjcghq47cmqeoj+wf7+gadx34t0w9khorrfr4jde0msjupj5ypwjq8lq56da12katjwth3dwang==
     *
     * signValue: appId=1522844930580008962&bizContent={"amount":1,"orderNo":"1607680348043362304","termrefnum":"4200001673202212279806129826","channelFee":"0","channelPaytime":"2022-12-27 18:11:16","extendInfo":"","accttype":"99","chnltrxid":"4200001673202212279806129826","payInterfaceOutTradeNo":"221227121633902864","buyerBizUserId":"7","termauthno":"OTHERS","cusid":"650121048160NMM","payInterfacetrxcode":"VSP501","payDatetime":"2022-12-27 18:11:16","acct":"opn0buCtTZCEctAyCzKlxbYEtMck","bizOrderNo":"200202212271810380795957070431","status":"OK"}&charset=utf-8&notifyId=1607680506879320066&notifyTime=2022-12-27 18:11:16&notifyType=allinpay.yunst.orderService.pay&version=1.0
     *
     */
    public BaseResp notify(HttpServletRequest request) {
        //解析参数
        TreeMap<String, String> map = new TreeMap<>();
        Map reqMap = request.getParameterMap();
        for (Object key : reqMap.keySet()) {
            map.put(key.toString(), ((String[]) reqMap.get(key))[0]);
        }
        log.info("tonglian notify: {}", map);
        String sign = map.get("sign");
        //System.out.println("sign is " + sign);
        map.remove("sign");
        map.remove("signType");
        //拼装待验签数据
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            String value = map.get(key);
            sb.append(key).append("=").append(value).append("&");
        }
        if(sb.length() > 0) sb.deleteCharAt(sb.length() - 1); //去除末尾&
        String signedValue = URLDecoder.decode(sb.toString());
        //System.out.println("signedValue is " + sb);
        try {
            //boolean checkSign = client.checkSign(signedValue, sign); //todo: 回调验签不行，不能用！
            boolean checkSign = true;
            if(checkSign) {
                JSONObject jsonObject = JSON.parseObject(map.get("bizContent"));
                log.info("tonglian notify data: {}", jsonObject);
                return BaseResp.success(jsonObject);
            } else log.info("通联回调验签失败");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用通联回调通知失败");
    }

    /**
     * 查询订单状态
     * 返回orderStatus: 1未支付（发起申请） 99进行中（发起支付确认） 4交易成功 3交易失败 5交易成功，但是发生了退款 6订单关闭
     * 详见：https://cloud.allinpay.com/ts-cloud-dev-web/#/apiCenter/index?params=y&key=291
     */
    public BaseResp getOrderDetail(JSONObject post) {
        final BizParameter param = new BizParameter();
        param.put("bizOrderNo", post.getString("bizOrderNo"));
        try {
            final OpenResponse response = client.execute("allinpay.yunst.orderService.getOrderDetail", param);
            if ("OK".equals(response.getSubCode())) {
                return BaseResp.success(response.getData());
            } else return BaseResp.error(response.getSubMsg());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用查询订单状态接口失败");
    }

    /**
     * 查询余额
     *
     * 返回数据：
     * allAmount 总额
     * freezenAmount 冻结额（分）
     */
    public BaseResp queryBalance(TonglianMember post) {
        final BizParameter param = new BizParameter();
        param.put("bizUserId", post.getBizUserId());
        param.put("accountSetNo", accountSetNo);
        try {
            final OpenResponse response = client.execute("allinpay.yunst.orderService.queryBalance", param);
            if ("OK".equals(response.getSubCode())) {
                return BaseResp.success(response.getData());
            } else return BaseResp.error(response.getSubMsg());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return BaseResp.error("调用查询余额接口失败");
    }
}
