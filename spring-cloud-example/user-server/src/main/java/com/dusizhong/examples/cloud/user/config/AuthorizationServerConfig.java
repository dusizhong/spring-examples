package com.dusizhong.examples.cloud.user.config;

import com.dusizhong.examples.cloud.user.model.BaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Resource
    DataSource dataSource;
    @Autowired
    RedisConnectionFactory connectionFactory;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    SmsUserDetailService smsUserDetailService;
    @Autowired
    MyTokenEnhancer myTokenEnhancer;

    private TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> granters = new ArrayList<TokenGranter>(Arrays.asList(endpoints.getTokenGranter()));
        granters.add(new SmsTokenGranter(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), smsUserDetailService));
        return new CompositeTokenGranter(granters);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager);
        endpoints.tokenGranter(tokenGranter(endpoints));
        // endpoints.tokenStore(new InMemoryTokenStore());
        // endpoints.tokenStore(new RedisTokenStore(connectionFactory));
        endpoints.tokenStore(new JdbcTokenStore(dataSource));
        endpoints.tokenEnhancer(myTokenEnhancer); //todo: 怎么无效了？！
        endpoints.exceptionTranslator(new CustomWebResponseExceptionTranslator());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.inMemory()
//                .withClient("test").secret("123")
//                .authorizedGrantTypes("password", "refresh_token", "sms")
//                .scopes("read", "write")
//                .accessTokenValiditySeconds(30)
//                .refreshTokenValiditySeconds(30);
        clients.withClientDetails(new JdbcClientDetailsService(dataSource));
    }

    //enable /oauth/check_token
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.checkTokenAccess("permitAll()");
    }

    private static class CustomWebResponseExceptionTranslator extends DefaultWebResponseExceptionTranslator {
        public ResponseEntity translate(Exception e) throws Exception {
            if (e instanceof OAuth2Exception) {
                if (e.getMessage().contains("Missing grant type")) {
                    return ResponseEntity.ok(BaseResp.error("缺少授权类型"));
                } else if (e.getMessage().contains("Unsupported grant type")) {
                    return ResponseEntity.ok(BaseResp.error("授权类型无效"));
                } else if (e.getMessage().contains("Unauthorized grant type")) {
                    return ResponseEntity.ok(BaseResp.error("客户端未授权此类型"));
                } else if (e.getMessage().contains("Bad credentials")) {
                    return ResponseEntity.ok(BaseResp.error("用户名或密码错误"));
                } else if (e.getMessage().contains("Token was not recognised")) {
                    return ResponseEntity.ok(BaseResp.error("无效的token"));
                }
            } else if(e instanceof InternalAuthenticationServiceException) {
                return ResponseEntity.ok((e.getMessage()));
            } else if(e instanceof HttpRequestMethodNotSupportedException) {
                return ResponseEntity.ok(BaseResp.error("请求方法错误"));
            }
            throw e;
        }
    }
}
