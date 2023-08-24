package com.dusizhong.examples.cloud.user.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws ServletException {
        Throwable cause = authException.getCause();
        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            if(cause == null) {
                response.getWriter().write("{\"code\":\"401\", \"message\":\"缺少token\", \"data\":null}");
            } else if(cause instanceof InvalidTokenException) {
                response.getWriter().write("{\"code\":\"401\", \"message\":\"token无效\", \"data\":null}");
            } else {
                response.getWriter().write("{\"code\":\"401\", \"message\":\"token异常\", \"data\":null}");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setStatus(401); // must be 401
    }
}