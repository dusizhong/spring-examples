package com.dusizhong.examples.cloud.user.config;

import com.dusizhong.examples.cloud.user.entity.SysUser;
import com.dusizhong.examples.cloud.user.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyTokenEnhancer implements TokenEnhancer {

    @Autowired
    private SysUserRepository sysUserRepository;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        SysUser sysUser = sysUserRepository.findByUsername(authentication.getName());
        final Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("role", sysUser.getRole());
        additionalInfo.put("enterpriseId", sysUser.getEnterpriseId());
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}
