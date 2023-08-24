package com.dusizhong.examples.cloud.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class RibbonUtils {

    public final static String USER_SERVER = "http://USER-SERVER";
    public final static String USER_SERVER_IP = "http://127.0.0.1:9090/user";

    @Autowired
    private RestTemplate restTemplate;

    public JSONObject postJson(String url, JSONObject params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.set("Authorization", "bearer " + Oauth2Utils.getAccessToken());
        HttpEntity<String> httpEntity = new HttpEntity<String>(params.toString(), httpHeaders);
        if(url.startsWith("http://1")) restTemplate = new RestTemplate(); //地址为IP时为普通http请求
        return restTemplate.postForEntity(url, httpEntity, JSONObject.class).getBody();
    }

    public JSONObject ribbonPost(String url, JSONObject params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.set("Authorization", "bearer " + Oauth2Utils.getAccessToken());
        HttpEntity<String> httpEntity = new HttpEntity<String>(params.toString(), httpHeaders);
        return restTemplate.postForEntity(url, httpEntity, JSONObject.class).getBody();
    }

    public JSONObject httpPost(String url, JSONObject params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.set("Authorization", "bearer " + Oauth2Utils.getAccessToken());
        HttpEntity<String> httpEntity = new HttpEntity<String>(params.toString(), httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(url, httpEntity, JSONObject.class).getBody();
    }
}
