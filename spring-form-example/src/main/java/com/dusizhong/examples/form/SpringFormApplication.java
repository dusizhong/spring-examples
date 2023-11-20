package com.dusizhong.examples.form;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SpringFormApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringFormApplication.class, args);
    }
}
