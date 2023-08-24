package com.dusizhong.examples.user.util.tonglian;

import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * 通联支付会员Post
 */
@Data
public class MemberPost {

    // 公共参数（所有接口）
    @NotNull
    private String bizUserId; //加密后用户id
    @NotNull
    private String timestamp; //时间戳，格式：yyMMddHHmmss
    @NotNull
    private String sign; //签名

    // 创建通联会员
    private Integer memberType; //用户类型（2企业会员 3个人会员）

    // 创建系统用户
    private String username;
    private String password;

    // 绑定手机
    private String phone; //手机号
    private String verificationCode; //短信码

    // 设置单位信息
    private String backUrl; //审核结果回传用户中心地址
    private String companyName; //企业名称，如有括号，用中文格式
    private String companyAddress; //企业地址
    private String uniCredit; //统一社会信用代码
    private String telephone; //联系电话
    private String legalName; //法人姓名
    private String legalIds; //法人证件号码 身份证
    private String legalPhone; //法人手机号码
    private String accountNo; //企业对公账户
    private String parentBankName; //开户银行名称需严格按照银行列表上送 https://cloud.allinpay.com/ts-cloud-dev-web/#/apiCenter/index?params=y&key=303
    private String bankName; //开户行支行名称 如：“中国工商银行股份有限公司北京樱桃园支行”
    private String unionBank; //支付行号，12位数字

    // 签约
    // 个人会员：名称
    // 企业会员：法人提现，则上送“法人姓名”，对公户提现，则上送“企业名称”
    private String signAcctName;
}
