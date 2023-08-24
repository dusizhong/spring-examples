package com.dusizhong.examples.cloud.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.cloud.user.model.BaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class Oauth2Controller {

    @Autowired
    @Qualifier("consumerTokenServices")
    private ConsumerTokenServices consumerTokenServices;

    @GetMapping("/principal")
    public Principal principal(Principal principal) {
        return principal;
    }

    @RequestMapping("/logout")
    public BaseResp logout() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if(details != null) {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(details);
            if (jsonObject != null) {
                String access_token = jsonObject.getString("tokenValue");
                if(!StringUtils.isEmpty(access_token)) consumerTokenServices.revokeToken(access_token);
                else return BaseResp.error("token为空");
            } else return BaseResp.error("解析令牌失败");
        } else return BaseResp.error("获取令牌失败");
        return BaseResp.error("注销成功");
    }
}
