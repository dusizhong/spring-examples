package com.dusizhong.examples.util.controller;

import com.dusizhong.examples.util.util.BrowserUtils;
import com.dusizhong.examples.util.util.IpUtils;
import com.dusizhong.examples.util.util.QrCodeUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/getIp")
    public String getIp(HttpServletRequest httpServletRequest) {
        System.out.println("ip信息：" + IpUtils.getIpAddr(httpServletRequest));
        return IpUtils.getIpAddr(httpServletRequest);
    }

    @RequestMapping("/getBrowser")
    public String getBrowser(HttpServletRequest httpServletRequest) {
        System.out.println("Browser信息：" + BrowserUtils.getBrowser(httpServletRequest));
        return BrowserUtils.getBrowser(httpServletRequest);
    }

    @RequestMapping("/generateQrCode")
    public String generateQrCode(@RequestParam String info) {
        return QrCodeUtils.generateQrCode(info);
    }
}
