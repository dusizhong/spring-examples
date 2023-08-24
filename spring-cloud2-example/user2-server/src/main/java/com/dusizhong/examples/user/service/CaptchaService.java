package com.dusizhong.examples.user.service;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.util.CaptchaUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 * @author Dusizhong
 * @since 2022-09-21
 */
@Service
public class CaptchaService {

    @Autowired
    private CaptchaUtils captchaUtils;

    //验证码缓存
    private Cache<String, Object> captchaCache = Caffeine.newBuilder()
            .maximumSize(1000) //缓存最大条数
            .expireAfterWrite(60, TimeUnit.SECONDS) //过期时间
            .build();

    /**
     * 生成图片验证码
     * @param sessionId
     * @return base64图片码
     */
    public String generateCaptcha(String sessionId) {
        JSONObject captcha = captchaUtils.createCaptcha();
        captchaCache.put(sessionId, captcha.getString("code"));
        return captcha.getString("pic");
    }

    /**
     * 校验图片验证码
     * @param sessionId
     * @param picCode
     * @return
     */
    public Boolean verifyCaptcha(String sessionId, String picCode) {
        Boolean result = false;
        String cacheCode = (String) captchaCache.getIfPresent(sessionId);
        if(!StringUtils.isEmpty(cacheCode)) {
            if(cacheCode.equals(picCode)) {
                result = true;
            }
        }
        return result;
    }
}
