package com.dusizhong.examples.cloud.user.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.Map;


public class SmsTokenGranter extends MyAbstractTokenGranter {

    private static final String GRANT_TYPE = "sms";
    private final SmsUserDetailService smsUserDetailService;

    public SmsTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, SmsUserDetailService smsUserDetailService) {
        super(tokenServices, clientDetailsService, requestFactory,GRANT_TYPE);
        this.smsUserDetailService = smsUserDetailService;
    }

    @Override
    protected UserDetails getUserDetails(Map<String, String> parameters) {
        String phone = parameters.get("phone");
        String code = parameters.get("code");
        return smsUserDetailService.loadUserByPhone(phone, code);
    }
}
