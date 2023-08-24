package com.dusizhong.examples.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.cloud.util.Oauth2Utils;
import com.dusizhong.examples.cloud.util.RibbonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.dusizhong.examples.cloud.util.RibbonUtils.USER_SERVER;

@RestController
public class BizController {

    @Autowired
    private RibbonUtils ribbonUtils;

    @RequestMapping("/test")
    public String test() {
        return Oauth2Utils.getCurrentUser().toJSONString();
    }

    @RequestMapping("/test1")
    public String test1() {
        JSONObject params = new JSONObject();
        params.put("id", "1");
        JSONObject result = ribbonUtils.postJson(USER_SERVER + "/enterprise/detail", params);
        if(result.get("code").equals(200)) {
            return result.getString("data");
        } else {
            System.err.println(result.toString());
            return null;
        }
    }
}
