package com.dusizhong.examples.http.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;

public class HttpUtils {

    public JSONObject get(String url) {
        // get access token
//        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
//        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
//        String access_token = jsonObject.getString("tokenValue");
        // prepare headers
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
        headers.setContentType(mediaType);
        headers.set("Authorization", "bearer " + "access_token");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        // set timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(180000);
        requestFactory.setReadTimeout(180000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        //restTemplate.setErrorHandler(new RestTemplateException());
        // send request
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            System.err.println(response.toString());
            return null;
        }
    }

    public static JSONObject get(String url, Map<String, String> params) {
        // get access token
//        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
//        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
//        String access_token = jsonObject.getString("tokenValue");
        // prepare headers
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
        headers.setContentType(mediaType);
        headers.set("Authorization", "bearer " + "access_token");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        // set timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(180000);
        requestFactory.setReadTimeout(180000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        //restTemplate.setErrorHandler(new RestTemplateException());
        // prepare the body params
        if (params.size() > 0) url = url + "?";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s", entry.getKey().toString(), entry.getValue().toString()));
        }
        url = url + sb;
        // send request
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        if (response.getStatusCode().is2xxSuccessful()) return response.getBody();
        else {
            System.out.println(response.getStatusCodeValue());
            return null;
        }
    }

    public static JSONObject postJson(String url, JSONObject params) {
        // get access token
//        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
//        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
//        String access_token = jsonObject.getString("tokenValue");
        // prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", "bearer " + "access_token");
        HttpEntity<String> httpEntity = new HttpEntity<String>(params.toString(), headers);
        // set timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(180000);
        requestFactory.setReadTimeout(180000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        // send request
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class);
        if (response.getStatusCode().is2xxSuccessful()) {
           System.out.println("response is " + response.getBody());
            return response.getBody();
        } else {
            System.out.println(response.toString());
            JSONObject result = new JSONObject();
            result.put("code", 500);
            result.put("message", response.toString());
            result.put("data", null);
            return result;
        }
    }

    public static JSONObject postMultipartFile(String url, String filePath) {
        // get access token
//        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
//        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(details);
//        String access_token = jsonObject.getString("tokenValue");
        // prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.set("Authorization", "bearer " + access_token);
//        headers.set("Authorization", "Bearer " + Oauth2Utils.getAccessToken());
        FileSystemResource resource = new FileSystemResource(new File(filePath));
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", resource);
        // form.add("fileName", "test.pdf");
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);
        // set timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(180000);
        requestFactory.setReadTimeout(180000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        JSONObject result = restTemplate.postForObject(url, entity, JSONObject.class);
        return result;
    }
}
