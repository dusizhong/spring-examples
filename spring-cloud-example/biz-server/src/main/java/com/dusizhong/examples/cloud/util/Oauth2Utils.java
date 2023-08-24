package com.dusizhong.examples.cloud.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        if(principal instanceof String) currentUser.put("id", principal); //未登录返回字符串anonymousUser
        else currentUser = (JSONObject) JSONObject.toJSON(principal);
        //System.out.println(currentUser);
        return currentUser;
    }

    public static String getAccessToken() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
        return jsonObject.getString("tokenValue");
    }
}