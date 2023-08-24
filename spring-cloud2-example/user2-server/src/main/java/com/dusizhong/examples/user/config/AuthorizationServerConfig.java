package com.dusizhong.examples.user.config;

import com.ezjc.user.config.custom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 授权服务器配置
 * @author Dusizhong
 * @since 2022-04-20
 * @update 2023-07-20 添加国资委code登录方式
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    DataSource dataSource;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    HebcaUserDetailService hebcaUserDetailService;
    @Autowired
    NixcaUserDetailService nixcaUserDetailService;
    @Autowired
    GzwUserDetailService gzwUserDetailService;

    @Primary
    @Bean
    public TokenStore jdbcTokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    //添加自定义授权方式
    private TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> granters = new ArrayList<>(Arrays.asList(endpoints.getTokenGranter()));
        granters.add(new HebcaTokenGranter(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), hebcaUserDetailService));
        granters.add(new NixcaTokenGranter(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), nixcaUserDetailService));
        granters.add(new GzwTokenGranter(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), gzwUserDetailService));
        return new CompositeTokenGranter(granters);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.tokenStore(new JdbcTokenStore(dataSource));
        endpoints.authenticationManager(authenticationManager);
        endpoints.tokenGranter(tokenGranter(endpoints));
    }

    //使用数据库客户端密钥
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.inMemory()
//                .withClient("test").secret(new BCryptPasswordEncoder().encode("123"))
//                .authorizedGrantTypes("password", "refresh_token", "hebca")
//                .scopes("read", "write")
//                .accessTokenValiditySeconds(30)
//                .refreshTokenValiditySeconds(30);
        clients.withClientDetails(new JdbcClientDetailsService(dataSource));
    }

    //enable the oauth/check_token for the resource server can get user info by token
//    @Override
//    public void configure(AuthorizationServerSecurityConfigurer security) {
//        security.allowFormAuthenticationForClients().checkTokenAccess("isAuthenticated()").tokenKeyAccess("permitAll()");
//    }
}
