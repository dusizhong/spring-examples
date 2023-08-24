package com.dusizhong.examples.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SpringHttpApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringHttpApplication.class, args);
    }

    @RequestMapping("/")
    public String greetings() {
        return "a spring http request example project";
    }
}
