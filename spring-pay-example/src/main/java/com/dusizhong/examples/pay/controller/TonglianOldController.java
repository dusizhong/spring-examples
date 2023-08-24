package com.dusizhong.examples.pay.controller;

import com.dusizhong.examples.pay.tonglian.TonglianOldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 通联收银宝支付
 */
@RestController
@RequestMapping("/tonglian/old")
public class TonglianOldController {

    @Autowired
    TonglianOldService tonglianService;

    private final static Logger logger = LoggerFactory.getLogger(TonglianOldController.class);

    /**
     * 网关支付
     *
     * @param principal
     * @param guaranteeId
     * @return
     */
//    @RequestMapping("/pay")
//    public Result pay(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
//                      @RequestParam(defaultValue = "0") Integer guaranteeId,
//                      @RequestParam String paytype) {
//        User user = userRepo.findByUsername(principal.getUsername());
//        Guarantee guarantee = guaranteeRepo.findOne(guaranteeId);
//        if (!user.getId().equals(guarantee.getUserId())) return new Result(false, "用户与保函不符", "");
//        if (guarantee == null) return new Result(false, "无此保函", "");
//        if (guarantee.getPaid().equals("已支付") || guarantee.getStatus().equals("PAID"))
//            return new Result(false, "此保函已支付", "");
//        if (guarantee.getPaid().equals("已过期")) return new Result(false, "此保函已过期", "");
//        if (!guarantee.getStatus().equals("SUBMIT")) return new Result(false, "保函状态不符", "");
//        String centTotalFee = Calculator.convertToCent(guarantee.getFee());//金额转换为分
//        if (StringUtils.isEmpty(centTotalFee)) return new Result(false, "金额转换失败", "");
//        /*Integer beforDate = beforDate(guarantee.getBidOpenDate());
//        if (beforDate >= 0) {
//            guarantee.setPaid("已过期");
//            guarantee.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//            guaranteeRepo.save(guarantee);
//            return new Result(false, "开标前36小时不可以支付", "");
//        }*/
//        String outTradeNo = guarantee.getSerialNo(); //使用： 20位保函编号 作为每次发起支付时新生成订单号(共20位)
//        String bidSectionName = guarantee.getBidSectionName();//商品描述信息
//        String random = RandomStringUtils.randomNumeric(10);
//        Map<String, String> result = tonglianService.tonglianpay(outTradeNo, centTotalFee, random, bidSectionName, paytype);
//        //生成预交易记录
//        TransRecord transRecord = transRecordRepo.findByOutTradeNo(outTradeNo);
//        if (transRecord == null) transRecord = new TransRecord();
//        transRecord.setUserId(user.getId());
//        transRecord.setOutTradeNo(outTradeNo);
//        transRecord.setBizId(guarantee.getSerialNo());
//        transRecord.setBizTitle(guarantee.getProjectName());
//        transRecord.setTotalFee(guarantee.getFee());
//        transRecord.setStatus("PENDING");
//        transRecord.setTradeType(paytype);
//        transRecord.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//        transRecordRepo.save(transRecord);
//        return new Result(true, "", result);
//    }
//
//    /**
//     * 支付回调
//     *
//     * @param request
//     * @param response
//     * @return
//     * @throws IOException
//     */
//    @Transactional
//    @RequestMapping("/notify")
//    public String tongliannotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String result = "fail";
//        request.setCharacterEncoding("utf-8");
//        TreeMap<String, String> params = getParams(request);
//        logger.info("支付回调结果:" + params);
//        try {
//            boolean isSign = validSign(params, TonglianService.APPKEY);// 接受到推送通知,首先验签
//            logger.info("支付回调验签结果:" + isSign + params.get("cusorderid"));
//            if (isSign) {
//                if (params.get("trxstatus").equals("0000")) {
//                    //获取交易记录
//                    TransRecord transRecord = transRecordRepo.findByOutTradeNo(params.get("cusorderid"));//业务流水(如订单号，保单号，缴费编号等)
//                    if (transRecord != null) {
//                        if (transRecord.getStatus().equals("SUCCESS")) { //处理notify重复发送
//                            result = "success";
//                        } else {
//                            //更新交易记录
//                            transRecord.setOutTradeNo(params.get("cusorderid"));
//                            transRecord.setMchId(params.get("cusid"));
//                            transRecord.setBuyerId(params.get("acct"));
//                            transRecord.setTransactionId(params.get("trxid"));
//                            transRecord.setStatus("SUCCESS");
//                            transRecord.setNotifyTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                            transRecordRepo.save(transRecord);
//                            Guarantee guarantee = guaranteeRepo.findBySerialNoAndStatus(params.get("cusorderid"), "SUBMIT");
//                            if (guarantee != null) {
//                                if ("SUBMIT".equals(guarantee.getStatus())) {
//                                    String transRecordfee = Calculator.convertToCent(transRecord.getTotalFee());//金额转换为分进行判断
//                                    if (params.get("trxamt").equals(transRecordfee)) {
//                                        //更新保函记录
//                                        guarantee.setStatus("PAID");
//                                        guarantee.setPaid("已支付");
//                                        guarantee.setStatusName("待出函");
//                                        guarantee.setPaidWay(transRecord.getTradeType());
//                                        guarantee.setTransactionId(params.get("trxid"));
//                                        guarantee.setPaidTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                                        guaranteeRepo.save(guarantee);
//                                        logger.info("notify支付成功" + params.get("cusorderid"));
//                                        //List<User> list = userRepo.findByGuarantorIdOrderByIdDesc(guarantee.getGuarantorId());
//                                        //smsService.qcloudSendSms(list.get(0).getPhone(), smsService.applyPaid);
//                                        result = "success";
//                                    } else logger.info("notify金额不符" + params.get("cusorderid"));
//                                } else logger.info("notify回调：保函状态不符" + params.get("cusorderid"));
//                            } else logger.info("notify获取保函记录失败" + params.get("cusorderid"));
//                        }
//                    } else logger.info("notify获取交易记录失败" + params.get("cusorderid"));
//                } else logger.info("交易结果码不正确" + params.get("cusorderid"));
//            } else logger.info("notify验签不通过" + params.get("cusorderid"));
//        } catch (Exception e) {//处理异常
//            // TODO: handle exception
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    /**
//     * 支付宝二维码支付
//     *
//     * @param principal
//     * @param guaranteeId
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping("/qr_code")
//    public Result qr_codepay(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
//                             @RequestParam(defaultValue = "0") Integer guaranteeId) throws Exception {
//        User user = userRepo.findByUsername(principal.getUsername());
//        Guarantee guarantee = guaranteeRepo.findOne(guaranteeId);
//        if (!user.getId().equals(guarantee.getUserId())) return new Result(false, "用户与保函不符", "");
//        if (guarantee == null) return new Result(false, "无此保函", "");
//        if (guarantee.getPaid().equals("已支付") || guarantee.getStatus().equals("PAID"))
//            return new Result(false, "此保函已支付", "");
//        if (guarantee.getPaid().equals("已过期")) return new Result(false, "此保函已过期", "");
//        if (!guarantee.getStatus().equals("SUBMIT")) return new Result(false, "保函状态不符", "");
//        String centTotalFee = Calculator.convertToCent(guarantee.getFee());//金额转换为分
//        if (StringUtils.isEmpty(centTotalFee)) return new Result(false, "金额转换失败", "");
//        /*Integer beforDate = beforDate(guarantee.getBidOpenDate());
//        if (beforDate >= 0) {
//            guarantee.setPaid("已过期");
//            guarantee.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//            guaranteeRepo.save(guarantee);
//            return new Result(false, "开标前36小时不可以支付", "");
//        }*/
//        String outTradeNo = guarantee.getSerialNo(); //使用： 20位保函编号 作为每次发起支付时新生成订单号(共20位)
//        String random = RandomStringUtils.randomNumeric(10);
//        //String body = guarantee.getBidSectionName() + guarantee.getType() + "服务费"; //body<128字符
//        //if (body.length() > 42) body = body.substring(0, 42);
//        String url = tonglianService.qr_codepay(centTotalFee, outTradeNo, random, "投标保函服务费");
//        if (StringUtils.isEmpty(url)) logger.info("发起支付宝支付请求失败");
//        String uuid = UUID.randomUUID() + ".png";
//        String folder = RES_PATH + "/qrcode/";
//        File targetFile = new File(folder);
//        if (!targetFile.exists()) {
//            targetFile.mkdirs();
//        }
//        if (!QRCodeUtil.zxingCodeCreate(url, 300, 300, folder + uuid, "png")) return new Result(false, "生成二维码失败", "");
//        String resul = RES_URL + "/qrcode/" + uuid;
//        //生成预交易记录
//        TransRecord transRecord = transRecordRepo.findByOutTradeNo(outTradeNo);
//        if (transRecord == null) transRecord = new TransRecord();
//        transRecord.setUserId(user.getId());
//        transRecord.setOutTradeNo(outTradeNo);
//        transRecord.setBizId(guarantee.getSerialNo());
//        transRecord.setBizTitle(guarantee.getProjectName());
//        transRecord.setTotalFee(guarantee.getFee());
//        transRecord.setStatus("PENDING");
//        transRecord.setTradeType("alipayqr");
//        transRecord.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//        transRecordRepo.save(transRecord);
//        return new Result(true, "", resul);
//    }
//
//    /**
//     * 微信二维码支付
//     *
//     * @param principal
//     * @param guaranteeId
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping("/weixin")
//    public Result weixin(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
//                         @RequestParam(defaultValue = "0") Integer guaranteeId) throws Exception {
//        User user = userRepo.findByUsername(principal.getUsername());
//        Guarantee guarantee = guaranteeRepo.findOne(guaranteeId);
//        if (!user.getId().equals(guarantee.getUserId())) return new Result(false, "用户与保函不符", "");
//        if (guarantee == null) return new Result(false, "无此保函", "");
//        if (guarantee.getPaid().equals("已支付") || guarantee.getStatus().equals("PAID"))
//            return new Result(false, "此保函已支付", "");
//        if (guarantee.getPaid().equals("已过期")) return new Result(false, "此保函已过期", "");
//        if (!guarantee.getStatus().equals("SUBMIT")) return new Result(false, "保函状态不符", "");
//        String centTotalFee = Calculator.convertToCent(guarantee.getFee());//金额转换为分
//        if (StringUtils.isEmpty(centTotalFee)) return new Result(false, "金额转换失败", "");
//        /*Integer beforDate = beforDate(guarantee.getBidOpenDate());
//        if (beforDate >= 0) {
//            guarantee.setPaid("已过期");
//            guarantee.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//            guaranteeRepo.save(guarantee);
//            return new Result(false, "开标前36小时不可以支付", "");
//        }*/
//        String outTradeNo = guarantee.getSerialNo(); //使用： 20位保函编号 作为每次发起支付时新生成订单号(共20位)
//        String random = RandomStringUtils.randomNumeric(10);
//        //String body = guarantee.getBidSectionName() + guarantee.getType() + "服务费"; //body<128字符
//        //if (body.length() > 42) body = body.substring(0, 42);
//        String url = tonglianService.tonglianweixin(centTotalFee, outTradeNo, "投标保函服务费", random);
//        if (StringUtils.isEmpty(url)) logger.info("发起微信支付请求失败");
//        String uuid = UUID.randomUUID() + ".png";
//        String folder = RES_PATH + "/tonglianweixin/";
//        File targetFile = new File(folder);
//        if (!targetFile.exists()) {
//            targetFile.mkdirs();
//        }
//        if (!QRCodeUtil.zxingCodeCreate(url, 300, 300, folder + uuid, "png")) return new Result(false, "生成二维码失败", "");
//        String resul = RES_URL + "/tonglianweixin/" + uuid;
//        //生成预交易记录
//        TransRecord transRecord = transRecordRepo.findByOutTradeNo(outTradeNo);
//        if (transRecord == null) transRecord = new TransRecord();
//        transRecord.setUserId(user.getId());
//        transRecord.setOutTradeNo(outTradeNo);
//        transRecord.setBizId(guarantee.getSerialNo());
//        transRecord.setBizTitle(guarantee.getProjectName());
//        transRecord.setTotalFee(guarantee.getFee());
//        transRecord.setStatus("PENDING");
//        transRecord.setTradeType("tonglianweixin");
//        transRecord.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//        transRecordRepo.save(transRecord);
//        return new Result(true, "", resul);
//    }
//
//    public static boolean validSign(TreeMap<String, String> param, String appkey) throws Exception {
//        if (param != null && !param.isEmpty()) {
//            if (!param.containsKey("sign")) {
//                return false;
//            }
//            param.put("key", appkey);//将分配的appkey加入排序
//            StringBuilder sb = new StringBuilder();
//            String sign = param.get("sign").toString();
//            param.remove("sign");
//            for (String key : param.keySet()) {
//                String value = param.get(key);
//                sb.append(key).append("=").append(value).append("&");
//            }
//            if (sb.length() > 0) {
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            String blank = sb.toString();
//            System.out.println(blank + ";" + sign);
//            return sign.toLowerCase().equals(TonglianService.md5(blank));
//        }
//        return false;
//    }
//
//    private TreeMap<String, String> getParams(HttpServletRequest request) {
//        TreeMap<String, String> map = new TreeMap<String, String>();
//        Map reqMap = request.getParameterMap();
//        for (Object key : reqMap.keySet()) {
//            map.put(key.toString(), ((String[]) reqMap.get(key))[0]);
//        }
//        return map;
//    }
//
//    /**
//     * 是否为开标时间前36小时
//     *
//     * @param date
//     * @return
//     * @throws ParseException
//     */
//    public static Integer beforDate(String date) throws ParseException {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置时间格式
//        Date parse = sdf.parse(date);
//        Date dBefore = new Date();
//        Calendar calendar = Calendar.getInstance(); //得到日历
//        calendar.setTime(parse);//把当前时间赋给日历
//        calendar.add(Calendar.HOUR, -36);  //设置为前两天
//        dBefore = calendar.getTime();   //得到前两天的时间
//        String defaultStartDate = sdf.format(dBefore);
//
//        System.out.println(defaultStartDate);
//        Date oldDate = sdf.parse(defaultStartDate);
//        // 当前时间
//        Date newOld = new Date();
//        newOld = sdf.parse(sdf.format(newOld));
//        // flag，等于0日期相等，大于0表示在当前时间之后，小于0在当前时间之前
//        int flag = newOld.compareTo(oldDate);
//        return flag;
//    }
}
