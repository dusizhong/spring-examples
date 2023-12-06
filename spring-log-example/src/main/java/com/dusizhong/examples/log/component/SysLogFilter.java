package com.dusizhong.examples.log.component;

import com.dusizhong.examples.log.entity.SysLog;
import com.dusizhong.examples.log.repository.SysLogRepository;
import com.dusizhong.examples.log.util.IpUtils;
import com.dusizhong.examples.log.util.Oauth2Utils;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * 自定义过滤器日志
 * 暂未使用，无法获取操作的模型
 * @author Dusizhong
 * @since 2023-12-04
 */
@Slf4j
//@Component
public class SysLogFilter extends OncePerRequestFilter {

    @Autowired
    private SysLogRepository sysLogRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(req, resp);

            byte[] requestBody = req.getContentAsByteArray();
            byte[] responseBody = resp.getContentAsByteArray();
            String requestData = new String(requestBody, StandardCharsets.UTF_8);
            log.info("request body = {}", requestData);
            log.info("response body = {}", new String(responseBody, StandardCharsets.UTF_8));

            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            OperatingSystem os = userAgent.getOperatingSystem();
            Browser browser = userAgent.getBrowser();
            String ip = IpUtils.getIpAddr(request);

            SysLog sysLog = new SysLog();
            sysLog.setOpUrl(request.getRequestURI());
            sysLog.setOpBiz("");
            sysLog.setOpBizId(Oauth2Utils.getCurrentUser().getString("id"));
            sysLog.setOpBizData(requestData);
            sysLog.setOpUserOs(os.getName());
            sysLog.setOpUserBrowser(browser.getName());
            sysLog.setOpUserIp(ip);
            sysLog.setOpUserId(Oauth2Utils.getCurrentUser().getString("id"));
            sysLog.setOpUserName(Oauth2Utils.getCurrentUser().getString("name"));
            sysLog.setOpPlatform("EBID_MAN");
            sysLog.setOpTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-HH-ss HH:mm:ss.SSS")));
            CompletableFuture.runAsync(() -> sysLogRepository.save(sysLog)).exceptionally(e -> {
                log.error("异步保存日志失败: " + e.getMessage());
                return null;
            });
        } finally {
            // Finally remember to respond to the client with the cached data.
            resp.copyBodyToResponse();
        }
    }
}
