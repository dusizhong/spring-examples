package com.dusizhong.examples.user.config.custom;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import java.util.Map;

/**
 * 自定义宁夏CA授权方式
 * @author Dusizhong
 * @since 2022-09-22
 */
public class NixcaTokenGranter extends MyAbstractTokenGranter {

    private static final String GRANT_TYPE = "nixca";
    private NixcaUserDetailService nixcaUserDetailService;

    public NixcaTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, NixcaUserDetailService nixcaUserDetailService) {
        super(tokenServices, clientDetailsService, requestFactory,GRANT_TYPE);
        this.nixcaUserDetailService = nixcaUserDetailService;
    }

    @Override
    protected UserDetails getUserDetails(Map<String, String> parameters) {
        String cert = parameters.get("cert");
        String random = parameters.get("random");
        String sign = parameters.get("sign");
        return nixcaUserDetailService.loadUserByCertAndRandomAndSign(cert, random, sign);
    }
}
