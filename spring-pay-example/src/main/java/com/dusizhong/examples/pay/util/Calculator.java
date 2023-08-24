package com.dusizhong.examples.pay.util;

import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {

    public static String add(String a, String b) {
        String result = "";
        try {
            BigDecimal a1 = new BigDecimal(a.trim());
            BigDecimal b1 = new BigDecimal(b.trim());
            BigDecimal ab = a1.add(b1);
            if(Calculator.isNotEmpty(ab.toString())) {
                DecimalFormat df = new DecimalFormat("0.00");
                result = df.format(ab);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 验证输入是否为非零金额
     * @param input
     * @return result
     */
    public static boolean isAmount(String input) {
        if(null == input || input.isEmpty()) {
            return false;
        }
        boolean result = false;
        Pattern pattern = Pattern.compile("\\d+(\\.\\d{1,2})?");
        Matcher matcher = pattern.matcher(input);
        if(matcher.matches()) {
            //非0检查
            BigDecimal inputDecimal = new BigDecimal(input);
            if(inputDecimal.compareTo(new BigDecimal("0")) > 0) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 验证输入是否为金额（包括0）
     * @param input
     * @return result
     */
    public static boolean isZeroAmount(String input) {
        if(null == input || input.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\d+(\\.\\d{1,2})?");
        Matcher matcher = pattern.matcher(input);
        if(matcher.matches()) return true;
        else return false;
    }

    /**
     * 验证输入为正整数
     * @param input
     * @return boolean
     */
    public static boolean isInt(String input) {
        boolean result = false;
        Pattern pattern = Pattern.compile("^[0-9]*[1-9][0-9]*$");
        Matcher matcher = pattern.matcher(input);
        if(matcher.matches()) result = true;
        return result;
    }

    /**
     * 将金额转换单位为分
     * (转换失败返回空字符串)
     * @param str
     * @return
     */
    public static String convertToCent(String str) {
        String result = "";
        try {
            BigDecimal a1 = new BigDecimal(str.trim());
            BigDecimal b1 = new BigDecimal("100");
            BigDecimal ab = a1.multiply(b1).setScale(0, BigDecimal.ROUND_HALF_UP);
            if(Calculator.isNotEmpty(ab.toString()) && !ab.toString().contains(".")) {
                result = ab.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isNotEmpty(String str) {
        if(null == str || str.isEmpty()) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(isAmount(null));
        System.out.println(isInt("1202"));
        System.out.println(convertToCent("1.08"));
        String redirectUri = "http://124.239.222.114:9011/#/login?token=9c9333f7-dabf-4894-b1b8-4689def81271";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("redirect_uri", redirectUri);
        System.out.println("jsonObject" + jsonObject.toJSONString());
        JSONObject json = (JSONObject) JSONObject.parse(jsonObject.toJSONString());
        System.out.println(json.getString("redirect_uri"));
        String idcardNo = "222426198912065611";
        System.out.println(idcardNo.substring(idcardNo.length() - 6));
    }
}
