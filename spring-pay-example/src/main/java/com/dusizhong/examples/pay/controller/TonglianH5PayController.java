package com.dusizhong.examples.pay.controller;

import com.dusizhong.examples.pay.tonglian.h5.H5PayService;
import com.dusizhong.examples.pay.tonglian.h5.PayUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 通联h5收银台
 */
@RestController
@RequestMapping("/tonglian/h5")
public class TonglianH5PayController {

    /**
     * 发起H5支付，返回支付URL地址
     * 用微信打开URL，微信支付
     * 用支付宝打开URL，支付宝支付
     * @return
     */
    @RequestMapping("/pay")
    public String pay() {
        String result = "fail";
        H5PayService h5PayService = new H5PayService();
        String tradeNo = String.valueOf(System.currentTimeMillis());
        try {
            result = h5PayService.pay(tradeNo, 1, "测试支付1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping("/h5/cancel")
    public String cancel() {
        String result = "fail";
        H5PayService h5PayService = new H5PayService();
        String tradeNo = String.valueOf(System.currentTimeMillis());
        try {
            result = h5PayService.cancel(tradeNo, 1, "111994120000916027");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 接收支付结果
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/notify")
    public void notify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("notify接收到通知结果：");
        try {
            request.setCharacterEncoding("gbk");
            response.setCharacterEncoding("gbk");
            TreeMap<String,String> params = getParams(request);//动态遍历获取所有收到的参数,此步非常关键,因为收银宝以后可能会加字段,动态获取可以兼容
            boolean isSign = PayUtil.validSign(params, H5PayService.MCH_KEY);
            System.out.println("notify验签结果:"+isSign);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally{//收到通知,返回success
            response.getOutputStream().write("success".getBytes());
            response.flushBuffer();
        }
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
