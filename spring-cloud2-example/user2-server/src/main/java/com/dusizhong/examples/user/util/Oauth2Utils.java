package com.dusizhong.examples.user.util;

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
 * @since 2022-08-18
 */
@Slf4j
@Component
public class Oauth2Utils implements PrincipalExtractor {

    private static final String PRINCIPAL = "principal";

    @Override
    public Object extractPrincipal(Map<String, Object> map) {
        return map.get(PRINCIPAL);
    }

    public static JSONObject getCurrentUser() {
        JSONObject currentUser = new JSONObject();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof String) currentUser.put("id", principal); //未登录返回字符串
        else currentUser = (JSONObject) JSONObject.toJSON(principal);
        return currentUser;
    }

    public static String getAccessToken() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
        return jsonObject.getString("tokenValue");
    }
}