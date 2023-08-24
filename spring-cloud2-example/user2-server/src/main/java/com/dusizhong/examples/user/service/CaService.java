package com.dusizhong.examples.user.service;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.user.entity.SysExpert;
import com.dusizhong.examples.user.util.HmacMD5;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CaService {

    private static final String CA_SIGN_SERVER = "http://47.93.149.252:9191/expert-sign";
    private static final String PLATFORM_CODE = "I1301000075";
    private static final String SIGN_KEY = "yqRAryH8pmjurS8lxFHNENQFShUh/BturiiZIHfxfLAjuicdhi8fm0260dbkVsc5XVEzlhfEiJuUWD0z0g4o0Q==";

    public JSONObject receiveExpert(SysExpert post) {
        String queryString = "idCard=" + post.getExpertIdCardNo()
                + "&phone="+post.getExpertPhone()
                + "&platformCode=" + PLATFORM_CODE
                + "&time=" + System.currentTimeMillis()
                + "&userName=" + post.getExpertName();
        String digestMsgB64 = "&digestMsgB64=" + HmacMD5.getMsg(SIGN_KEY, queryString);
        queryString = queryString + digestMsgB64;
        HttpHeaders httpHeaders = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
        httpHeaders.setContentType(mediaType);
        HttpEntity<String> httpEntity = new HttpEntity<String>(queryString, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(CA_SIGN_SERVER + "/api/receiveExpert", httpEntity, JSONObject.class);
        if(response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            log.error(response.toString());
            return null;
        }
    }
}
