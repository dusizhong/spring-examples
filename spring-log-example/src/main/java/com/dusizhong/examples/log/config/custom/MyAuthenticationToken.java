package com.dusizhong.examples.log.config.custom;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 自定义授权令牌
 * 用于实现自定义授权方式
 * @author Dusizhong
 * @since 2023-09-20
 */
public class MyAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    public MyAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal, Object credentials) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
    }

    @Override
    public Object getCredentials() {
        return principal;
    }

    @Override
    public Object getPrincipal() {
        return credentials;
    }
}
