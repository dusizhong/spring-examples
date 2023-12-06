package com.dusizhong.examples.log.component;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.log.entity.SysLog;
import com.dusizhong.examples.log.repository.SysLogRepository;
import com.dusizhong.examples.log.util.IpUtils;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 登录日志
 * 注意：实际是用户带有token访问任何接口前都会检查token，从而触发此监听事件
 * @author Dusizhong
 * @since 2023-12-05
 */
@Slf4j
@Component
public class LoginEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private SysLogRepository sysLogRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
        Authentication authentication = authenticationSuccessEvent.getAuthentication();
        Object principal = authentication.getPrincipal();
        //String username = principal instanceof UserDetails ? ((UserDetails) principal).getUsername() : principal.toString();
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        OperatingSystem os = userAgent.getOperatingSystem();
        Browser browser = userAgent.getBrowser();
        String ip = IpUtils.getIpAddr(request);
        JSONObject currentUser = (JSONObject) JSONObject.toJSON(principal);
        //log.info("用户认证成功: {}, IP地址: {}, 浏览器信息: {}", request.getRequestURI(), ip, userAgent);

        if(request.getRequestURI().endsWith("/oauth/token")) {
            SysLog sysLog = new SysLog();
            sysLog.setOpUrl(request.getRequestURI());
            sysLog.setOpBiz("Oauth");
            sysLog.setOpBizId(currentUser.getString("id"));
            sysLog.setOpBizData(null);
            sysLog.setOpStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            sysLog.setOpEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            sysLog.setOpBizResult(currentUser.toJSONString());
            sysLog.setOpUserId(currentUser.getString("id"));
            sysLog.setOpUserName(currentUser.getString("name"));
            sysLog.setOpUserIp(ip);
            sysLog.setOpUserOs(os.getName());
            sysLog.setOpUserBrowser(browser.getName());
            sysLog.setOpPlatform("LOG_SERVER");
            sysLog.setOpTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            CompletableFuture.runAsync(() -> sysLogRepository.save(sysLog)).exceptionally(e -> {
                log.error("异步保存日志失败: " + e.getMessage());
                return null;
            });
        }
    }
}
