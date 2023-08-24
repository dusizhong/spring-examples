package com.dusizhong.examples.cloud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/v2/api-docs", "/swagger-resources/**", "/doc.html**", "/webjars/**", "favicon.ico").permitAll()
                .antMatchers("/test/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated();
    }
}
