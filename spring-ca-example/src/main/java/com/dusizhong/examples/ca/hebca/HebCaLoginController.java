package com.dusizhong.examples.ca.hebca;

import com.hebca.pki.HebcaVerify;
import com.hebca.pki.RandomGen;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/hebca")
public class HebCaLoginController {

    @CrossOrigin("*")
    @RequestMapping("/random")
    public String random(HttpServletRequest request) {
        RandomGen mRandomGen = new RandomGen();
        String randomString = mRandomGen.getstrRandom();
        request.getSession().setAttribute("randomString",  randomString);
        return randomString;
    }

    @CrossOrigin("*")
    @PostMapping("/login")
    public String detail(@RequestParam String cert, @RequestParam String signData, HttpServletRequest request) {
        String result;
        String randomString = request.getSession().getAttribute("randomString").toString();
        try {
            HebcaVerify.getInstance().verifyCertSign(randomString, cert, signData);
            result = "登录成功";
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }
}
