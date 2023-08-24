package com.dusizhong.examples.cloud.user.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class MyAbstractTokenGranter extends AbstractTokenGranter {

    protected MyAbstractTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap(tokenRequest.getRequestParameters());
        UserDetails details = getUserDetails(parameters);
        if (details == null) {
            throw new InvalidGrantException("could not get user details");
        }
        MyAuthenticationToken authentication = new MyAuthenticationToken(details.getAuthorities(),parameters, details);
        authentication.setAuthenticated(true);
        authentication.setDetails(details);
        OAuth2Request storedOAuth2Request = this.getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, authentication);
    }

    protected abstract UserDetails getUserDetails(Map<String, String> parameters);
}
