package com.dusizhong.examples.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/avatar-res/**").permitAll()
                .antMatchers("/enterprise-res/**").permitAll()
                .antMatchers("/captcha/generate", "/sms/send").permitAll()
                .antMatchers("/area/list", "/area/tree", "/industry/tree").permitAll()
                .antMatchers("/register", "/resetPassword").permitAll()
                .antMatchers("/expert/createExpertUser").hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENCY")
                .antMatchers("/expert/deleteExpertUser").hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENCY")
                .antMatchers("/enterprise/approval").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                .antMatchers("/group/**").hasAuthority("ROLE_SUPER")
                .antMatchers("/gzw/**").permitAll()
                .anyRequest().authenticated();
    }
}
