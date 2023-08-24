package com.dusizhong.examples.user.util.tonglian;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 通联企业信息Post
 */
@Data
public class CompanyPost {

    @NotNull
    private String bizUserId; //加密后用户id
    @NotNull
    private String timestamp; //时间戳，格式：yyMMddHHmmss
    @NotNull
    private String sign; //签名

    @NotNull
    private String companyName; //企业名称，如有括号，用中文格式
    @NotNull
    private String companyAddress; //企业地址
    @NotNull
    private String uniCredit; //统一社会信用代码
    @NotNull
    private String telephone; //联系电话
    @NotNull
    private String legalName; //法人姓名
    @NotNull
    private String legalIds; //法人证件号码 身份证
    @NotNull
    private String legalPhone; //法人手机号码
    @NotNull
    private String accountNo; //企业对公账户
    @NotNull
    private String parentBankName; //开户银行名称需严格按照银行列表上送 https://cloud.allinpay.com/ts-cloud-dev-web/#/apiCenter/index?params=y&key=303
    @NotNull
    private String bankName; //开户行支行名称 如：“中国工商银行股份有限公司北京樱桃园支行”
    @NotNull
    private String unionBank; //支付行号，12位数字

    private String backUrl; //审核结果回传用户中心地址

}
