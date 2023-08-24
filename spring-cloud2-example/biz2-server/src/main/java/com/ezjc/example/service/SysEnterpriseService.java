package com.ezjc.example.service;

import com.alibaba.fastjson.JSONObject;
import com.ezjc.example.util.RibbonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SysEnterpriseService {

    @Autowired
    private RibbonUtils ribbonUtils;

    //用户中心地址
    private final static String USER_SERVER = "http://USER-SERVER";

    public JSONObject findById(String enterpriseId) {
        JSONObject params = new JSONObject();
        params.put("id", enterpriseId);
        JSONObject result = ribbonUtils.postJson(USER_SERVER + "/enterprise/detail", params);
        if(result.get("code").equals(200)) {
            return result.getJSONObject("data");
        } else {
            log.info(result.toString());
            return null;
        }
    }
}
