package com.dusizhong.examples.user.config.custom;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.Map;

/**
 * 自定义国资委授权方式
 * @author Dusizhong
 * @since 2023-07-20
 */
public class GzwTokenGranter extends MyAbstractTokenGranter {

    private static final String GRANT_TYPE = "gzw";
    private GzwUserDetailService gzwUserDetailService;

    public GzwTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, GzwUserDetailService gzwUserDetailService) {
        super(tokenServices, clientDetailsService, requestFactory,GRANT_TYPE);
        this.gzwUserDetailService = gzwUserDetailService;
    }

    @Override
    protected UserDetails getUserDetails(Map<String, String> parameters) {
        String code = parameters.get("code");
        String userId = parameters.get("userId");
        String loginname = parameters.get("loginname");
        String companyNO = parameters.get("companyNO");
        return gzwUserDetailService.loadUserByCode(code, userId, loginname, companyNO);
    }
}
