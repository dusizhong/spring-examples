package com.dusizhong.examples.util.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoneyToChineseUtil {
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("^(0|[1-9]\\d{0,11})\\.(\\d\\d)$"); // 不考虑分隔符的正确性
    private static final char[] RMB_NUMS = "零壹贰叁肆伍陆柒捌玖".toCharArray();
    private static final String[] UNITS = {"元", "角", "分", "整"};
    private static final String[] U1 = {"", "拾", "佰", "仟"};
    private static final String[] U2 = {"", "万", "亿"};

    /**
     * 将金额（整数部分等于或少于12位，小数部分2位）转换为中文大写形式.
     *
     * @param amount 金额数字
     * @return 中文大写
     * @throws IllegalArgumentException
     */
    public static String convert(String amount) throws IllegalArgumentException {
        // 去掉分隔符
        if (!amount.contains(".")){
            amount =  amount + ".00";
        }
        amount = amount.replace(",", "");
        // 验证金额正确性
        if (amount.equals("0.00")) {
            return ("金额不能为零.");
        }
        Matcher matcher = AMOUNT_PATTERN.matcher(amount);
        if (!matcher.find()) {
            return ("输入金额有误.");
        }

        String integer = matcher.group(1); // 整数部分
        String fraction = matcher.group(2); // 小数部分

        String result = "";
        if (!integer.equals("0")) {
            result += integer2rmb(integer) + UNITS[0]; // 整数部分
        }
        if (fraction.equals("00")) {
            result += UNITS[3]; // 添加[整]
        } else if (fraction.startsWith("0") && integer.equals("0")) {
            result += fraction2rmb(fraction).substring(1); // 去掉分前面的[零]
        } else {
            result += fraction2rmb(fraction); // 小数部分
        }

        return result;
    }

    // 将金额小数部分转换为中文大写
    private static String fraction2rmb(String fraction) {
        char jiao = fraction.charAt(0); // 角
        char fen = fraction.charAt(1); // 分
        return (RMB_NUMS[jiao - '0'] + (jiao > '0' ? UNITS[1] : ""))
                + (fen > '0' ? RMB_NUMS[fen - '0'] + UNITS[2] : "");
    }

    // 将金额整数部分转换为中文大写
    private static String integer2rmb(String integer) {
        StringBuilder buffer = new StringBuilder();
        // 从个位数开始转换
        int i, j;
        for (i = integer.length() - 1, j = 0; i >= 0; i--, j++) {
            char n = integer.charAt(i);
            if (n == '0') {
                // 当n是0且n的右边一位不是0时，插入[零]
                if (i < integer.length() - 1 && integer.charAt(i + 1) != '0') {
                    buffer.append(RMB_NUMS[0]);
                }
                // 插入[万]或者[亿]
                if (j % 4 == 0) {
                    if (i > 0 && integer.charAt(i - 1) != '0'
                            || i > 1 && integer.charAt(i - 2) != '0'
                            || i > 2 && integer.charAt(i - 3) != '0') {
                        buffer.append(U2[j / 4]);
                    }
                }
            } else {
                if (j % 4 == 0) {
                    buffer.append(U2[j / 4]);     // 插入[万]或者[亿]
                }
                buffer.append(U1[j % 4]);         // 插入[拾]、[佰]或[仟]
                buffer.append(RMB_NUMS[n - '0']); // 插入数字
            }
        }
        return buffer.reverse().toString();
    }

    /**
     * 对金额的格式调整到分
     *
     * @param money
     * @return
     */
    public static String moneyFormat(String money) {//23->23.00
        StringBuffer sb = new StringBuffer();
        if (money == null) {
            return "0.00";
        }
        int index = money.indexOf(".");
        if (index == -1) {
            return money + ".00";
        } else {
            String s0 = money.substring(0, index);//整数部分
            String s1 = money.substring(index + 1);//小数部分
            if (s1.length() == 1) {//小数点后一位
                s1 = s1 + "0";
            } else if (s1.length() > 2) {//如果超过3位小数，截取2位就可以了
                s1 = s1.substring(0, 2);
            }
            sb.append(s0);
            sb.append(".");
            sb.append(s1);
        }
        return sb.toString();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(MoneyToChineseUtil.moneyFormat("123.2803"));
        System.out.println(MoneyToChineseUtil.convert("500000.03"));
    }

}
