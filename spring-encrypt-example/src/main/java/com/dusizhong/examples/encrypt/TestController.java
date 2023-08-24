package com.dusizhong.examples.encrypt;

import com.dusizhong.examples.encrypt.util.IpUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class TestController {

    @RequestMapping(value = "/getIp")
    public String getIp(HttpServletRequest request) {
        System.out.println(request);
        return IpUtils.getIpAddr(request);
    }
}
