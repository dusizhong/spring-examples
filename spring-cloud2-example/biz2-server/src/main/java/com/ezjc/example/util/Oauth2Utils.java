package com.ezjc.example.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Oauth2工具类
 * 自定义PrincipalExtractor
 * 实现SecurityContextHolder获取用户信息
 * @author Dusizhong
 * @since 2022-04-27
 */
@Slf4j
@Component
public class Oauth2Utils implements PrincipalExtractor {

    private static final String PRINCIPAL = "principal";

    @Override
    public Object extractPrincipal(Map<String, Object> map) {
        return map.get(PRINCIPAL);
    }

    /**
     * 获取当前用户
     * @return JSONObject
     */
    public static JSONObject getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JSONObject user = (JSONObject) JSONObject.toJSON(principal);
        log.info(user.toString());
        return user;
    }

    /**
     * 获取访问token
     * @return String
     */
    public static String getAccessToken() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
        return jsonObject.getString("tokenValue");
    }
}