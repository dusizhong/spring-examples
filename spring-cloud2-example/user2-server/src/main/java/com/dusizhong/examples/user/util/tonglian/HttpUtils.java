package com.dusizhong.examples.user.util.tonglian;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.model.Resp;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;


/**
 * Http工具类
 */
public class HttpUtils {

    /**
     * Http Post请求
     * @param url
     * @param params
     * @return resp
     */
    public static Resp post(String url, JSONObject params) {

        //设置请求类型
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        //参数封装成实体类
        HttpEntity<String> httpEntity = new HttpEntity<String>(params.toString(), httpHeaders);
        //发送请求
        RestTemplate restTemplate = new RestTemplate();
        JSONObject json = restTemplate.postForEntity(TonglianUtils.BASE_URL + url, httpEntity, JSONObject.class).getBody();
        return new Resp(json.getInteger("code"), json.getString("message"), json.get("data"));
    }

    public static Resp upload(String url, String bizUserId, String timestamp, String sign, String filePath, String picType, String ocrComparisonResultBackUrl) {
        // 上传文件
        FileSystemResource fileSystemResource = new FileSystemResource(new File(filePath));
        if (!fileSystemResource.exists()) {
            System.out.println("文件不存在处理");
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>();
        form.add("bizUserId", bizUserId);
        form.add("timestamp", timestamp);
        form.add("sign", sign);
        form.add("file", fileSystemResource);
        form.add("picType", picType);
        form.add("ocrComparisonResultBackUrl", ocrComparisonResultBackUrl);
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        JSONObject json = restTemplate.postForEntity(TonglianUtils.BASE_URL + url, files, JSONObject.class).getBody();
        return new Resp(json.getInteger("code"), json.getString("message"), json.get("data"));
    }
}
