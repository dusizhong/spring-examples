package com.dusizhong.examples.user.config.custom;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.Map;

/**
 * 自定义河北CA授权方式
 * @author Dusizhong
 * @since 2022-04-20
 */
public class HebcaTokenGranter extends MyAbstractTokenGranter {

    private static final String GRANT_TYPE = "hebca";
    private HebcaUserDetailService hebcaUserDetailService;

    public HebcaTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, HebcaUserDetailService hebcaUserDetailService) {
        super(tokenServices, clientDetailsService, requestFactory,GRANT_TYPE);
        this.hebcaUserDetailService = hebcaUserDetailService;
    }

    @Override
    protected UserDetails getUserDetails(Map<String, String> parameters) {
        String cert = parameters.get("cert");
        String random = parameters.get("random");
        String sign = parameters.get("sign");
        return hebcaUserDetailService.loadUserByCertAndRandomAndSign(cert, random, sign);
    }
}
