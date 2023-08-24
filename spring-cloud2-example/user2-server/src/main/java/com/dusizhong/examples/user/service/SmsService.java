package com.dusizhong.examples.user.service;

import com.dusizhong.examples.user.util.TencentSmsUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务
 * @author Dusizhong
 * @since 2022-09-21
 */
@Service
public class SmsService {

    @Autowired
    private TencentSmsUtils tencentSmsUtils;

    //过期时间（秒）
    public final static int EXPIRED_SECONDS = 300;

    //验证码缓存
    private Cache<String, Object> smsCodeCache = Caffeine.newBuilder()
            .maximumSize(1000) //缓存最大条数
            .expireAfterWrite(EXPIRED_SECONDS, TimeUnit.SECONDS)
            .build();

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    public Boolean sendSmsCode(String phone) {
        Boolean result = false;
        String code = (String) smsCodeCache.getIfPresent(String.valueOf(phone));
        if(StringUtils.isEmpty(code)) {
            code = RandomStringUtils.random(4, false, true); //生成4位数字验证码
            smsCodeCache.put(phone, code);
            String[] templateParams = {code};
            String[] phoneNumbers = {phone};
            tencentSmsUtils.sendSms(TencentSmsUtils.CODE_TEMPLATE_ID, templateParams, phoneNumbers); //发送短信
            result = true;
        }
        return result;
    }

    /**
     * 校验短信验证码
     * @param phone
     * @param smsCode
     * @return
     */
    public Boolean verifySmsCode(String phone, String smsCode) {
        Boolean result = false;
        String cacheCode = (String) smsCodeCache.getIfPresent(String.valueOf(phone));
        if(!StringUtils.isEmpty(cacheCode)) {
            if(cacheCode.equals(smsCode)) {
                result = true;
            }
        }
        return result;
    }
}
