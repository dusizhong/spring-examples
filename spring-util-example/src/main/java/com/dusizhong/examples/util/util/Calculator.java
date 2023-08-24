package com.dusizhong.examples.util.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 浮点计算器
 * Created by stonedu on 2019/2/5.
 */
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

    public static String subtract(String a, String b) {
        String result = "";
        try {
            BigDecimal a1 = new BigDecimal(a.trim());
            BigDecimal b1 = new BigDecimal(b.trim());
            BigDecimal ab = a1.subtract(b1);
            if(Calculator.isNotEmpty(ab.toString())) {
                DecimalFormat df = new DecimalFormat("0.00");
                result = df.format(ab);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String multiply(String a, String b) {
        String result = "";
        try {
            BigDecimal a1 = new BigDecimal(a.trim());
            BigDecimal b1 = new BigDecimal(b.trim());
            BigDecimal ab = a1.multiply(b1);
            if(Calculator.isNotEmpty(ab.toString())) {
                DecimalFormat df = new DecimalFormat("0.00");
                result = df.format(ab);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String divide(String a, String b) {
        String result = "";
        try {
            BigDecimal a1 = new BigDecimal(a.trim());
            BigDecimal b1 = new BigDecimal(b.trim());
            BigDecimal ab = a1.divide(b1, 6, BigDecimal.ROUND_HALF_UP); //四舍五入，精确到分
            if(Calculator.isNotEmpty(ab.toString())) {
                DecimalFormat df = new DecimalFormat("0.00####");
                result = df.format(ab);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String convertToPercent(String str) {
       return new DecimalFormat("0.##%").format(new BigDecimal(str.trim()));
    }


    /**
     * 将金额转换单位为分(转换失败返回空字符串)
     * 用于匹配微信支付交易单位
     * @param str
     * @return
     */
    public static String convertToCent(String str) {
        String result = "";
        try {
            BigDecimal a1 = new BigDecimal(str.trim());
            BigDecimal b1 = new BigDecimal("100");
            BigDecimal ab = a1.multiply(b1).setScale(0, BigDecimal.ROUND_HALF_DOWN);
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
        System.out.println(Calculator.add("0", "0.5"));
        System.out.println(Calculator.subtract("2.67", "13.67"));
        System.out.println(Calculator.multiply("0.067", "13.67"));
        System.out.println(Calculator.divide("3.3", "113.67"));
        System.out.println(Calculator.convertToPercent("0.00035"));
    }
}
