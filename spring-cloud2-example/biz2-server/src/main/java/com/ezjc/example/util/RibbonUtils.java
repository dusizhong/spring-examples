package com.ezjc.example.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RibbonUtils {

    @Autowired
    RestTemplate restTemplate;

    /**
     * Http post请求
     * @param url
     * @param params
     * @return JSONObject
     */
    public JSONObject postJson(String url, JSONObject params) {
        //设置请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.set("Authorization", "bearer " + Oauth2Utils.getAccessToken());
        //封装参数
        HttpEntity<String> httpEntity = new HttpEntity<String>(params.toString(), httpHeaders);
        //发送请求
        return restTemplate.postForEntity(url, httpEntity, JSONObject.class).getBody();
    }
}
