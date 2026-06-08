package com.medical.framework.aspect;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 操作日志AOP切面（自动脱敏密码等敏感字段）
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /** 需要脱敏的参数字段名 */
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "password", "oldPassword", "newPassword", "pwd",
            "secret", "token", "accessToken", "refreshToken"
    ));

    @Pointcut("execution(* com.medical..controller..*.*(..))")
    public void controllerLog() {}

    @Around("controllerLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String method = request.getMethod();
        String url = request.getRequestURI();

        // 记录请求信息（参数已脱敏）
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("method", method);
        logMap.put("url", url);
        logMap.put("class", signature.getDeclaringTypeName());
        logMap.put("methodName", signature.getName());

        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            Map<String, Object> params = new HashMap<>();
            String[] paramNames = signature.getParameterNames();
            for (int i = 0; i < args.length; i++) {
                if (!(args[i] instanceof HttpServletRequest) &&
                    !(args[i] instanceof javax.servlet.http.HttpServletResponse)) {
                    params.put(paramNames[i], maskSensitive(args[i]));
                }
            }
            logMap.put("params", params);
        }

        log.info("API请求 -> {}", JSONUtil.toJsonStr(logMap));

        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("API响应 -> {} {} | 耗时: {}ms", method, url, elapsedTime);
            return result;
        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("API异常 -> {} {} | 耗时: {}ms | 异常: {}", method, url, elapsedTime, e.getMessage());
            throw e;
        }
    }

    /**
     * 对敏感字段进行脱敏处理
     */
    private Object maskSensitive(Object obj) {
        if (obj == null) {
            return null;
        }
        // 如果是Map类型，脱敏敏感Key
        if (obj instanceof Map) {
            Map<String, Object> masked = new HashMap<>((Map<String, Object>) obj);
            for (String key : masked.keySet()) {
                if (SENSITIVE_FIELDS.contains(key)) {
                    masked.put(key, "***");
                }
            }
            return masked;
        }
        // 如果是字符串且Key是敏感字段（由上游Map处理，此处做兜底）
        return obj;
    }
}
