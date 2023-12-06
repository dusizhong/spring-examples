package com.dusizhong.examples.log.component;

import com.alibaba.fastjson.JSONObject;
import com.dusizhong.examples.log.entity.SysLog;
import com.dusizhong.examples.log.repository.SysLogRepository;
import com.dusizhong.examples.log.util.IpUtils;
import com.dusizhong.examples.log.util.Oauth2Utils;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * 自定义切面日志
 * @author Dusizhong
 * @since 2023-12-05
 */
@Slf4j
@Aspect
@Component
public class SysLogAspect {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private SysLogRepository sysLogRepository;

    //@AfterReturning在目标执行有返回结果后切入
    @AfterReturning(pointcut = "execution(* com.dusizhong.examples.log.controller.*.*(..))", returning = "result")
    public void afterReturn(Object result) {
        log.info("Method execution successful with result: {}", result);
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(result);
        if("200".equals(jsonObject.getString("code"))) {
            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            OperatingSystem os = userAgent.getOperatingSystem();
            Browser browser = userAgent.getBrowser();
            String ip = IpUtils.getIpAddr(request);
            String url = request.getRequestURI();
            log.info("200" + url + browser + os + ip);

        }
    }

    //@Around可在目标执行前和执行前后切入
    //@Around("@annotation(com.dusizhong.examples.log.component.SysLoggable)") //@SysLoggable使用自定注解方式指定记录点
    @Around("execution(* com.dusizhong.examples.log.controller.*.*(..))") //匹配方式记录
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {

        if(!request.getRequestURI().endsWith("/list") && !request.getRequestURI().endsWith("/detail")) {
            //目标执行前
            Object[] args = joinPoint.getArgs();
            JSONObject jsonArgs = (JSONObject) JSONObject.toJSON(args[0]);
            String biz = args[0].getClass().getSimpleName();
            String bizId = null;
            if (jsonArgs.containsKey("id")) bizId = jsonArgs.getString("id");
            String bizData = jsonArgs.toJSONString();
            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            OperatingSystem os = userAgent.getOperatingSystem();
            Browser browser = userAgent.getBrowser();
            String ip = IpUtils.getIpAddr(request);
            String url = request.getRequestURI();
            String bizStartTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            //目标执行后
            Object result = joinPoint.proceed();
            JSONObject jsonResult = (JSONObject) JSONObject.toJSON(result);
            String bizEndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            if ("200".equals(jsonResult.getString("code"))) {
                SysLog sysLog = new SysLog();
                sysLog.setOpUrl(url);
                sysLog.setOpBiz(biz);
                sysLog.setOpBizId(bizId);
                sysLog.setOpBizData(bizData);
                sysLog.setOpStartTime(bizStartTime);
                sysLog.setOpEndTime(bizEndTime);
                sysLog.setOpBizResult(jsonResult.getString("data"));
                sysLog.setOpUserId(Oauth2Utils.getCurrentUser().getString("id"));
                sysLog.setOpUserName(Oauth2Utils.getCurrentUser().getString("name"));
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
            return result;
        } else {
            return joinPoint.proceed();
        }
    }
}
