package com.bookstore.common.aspect;

import com.bookstore.common.annotation.Log;
import com.bookstore.mapper.OperationLogMapper;
import com.bookstore.model.entity.OperationLog;
import com.bookstore.security.SecuritySupport;
import com.bookstore.util.AppUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Around("execution(* com.bookstore.controller..*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        Log logAnnotation = resolveLogAnnotation(joinPoint);
        boolean shouldRecord = logAnnotation != null || "GET".equalsIgnoreCase(request.getMethod());
        if (!shouldRecord) {
            return joinPoint.proceed();
        }
        LocalDateTime startTime = LocalDateTime.now();
        long startTimestamp = System.currentTimeMillis();

        Object result = null;
        Throwable throwable = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            throwable = e;
            throw e;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            long endTimestamp = System.currentTimeMillis();
            long duration = endTimestamp - startTimestamp;

            saveLog(joinPoint, logAnnotation, startTime, endTime, duration, result, throwable);
        }
    }

    private Log resolveLogAnnotation(ProceedingJoinPoint joinPoint) {
        if (!(joinPoint.getSignature() instanceof MethodSignature)) {
            return null;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log annotation = method.getAnnotation(Log.class);
        if (annotation != null) {
            return annotation;
        }
        try {
            Method implMethod = joinPoint.getTarget()
                    .getClass()
                    .getMethod(method.getName(), method.getParameterTypes());
            return implMethod.getAnnotation(Log.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, Log logAnnotation, 
                         LocalDateTime startTime, LocalDateTime endTime, long duration, 
                         Object result, Throwable throwable) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            HttpServletRequest request = attributes.getRequest();

            OperationLog opLog = new OperationLog();
            opLog.id = AppUtils.nextId();
            opLog.startTime = startTime;
            opLog.endTime = endTime;
            opLog.durationMs = duration;
            opLog.path = request.getRequestURI();
            opLog.method = request.getMethod() + " " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
            opLog.ip = getClientIp(request);

            // 获取当前用户 (不抛异常，可能未登录)
            try {
                SecuritySupport.LoginUser user = SecuritySupport.current();
                if (user != null) {
                    opLog.userId = user.userId;
                    opLog.username = user.username;
                }
            } catch (Exception ignored) {
            }

            // 参数序列化
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    opLog.params = objectMapper.writeValueAsString(args);
                } catch (Exception e) {
                    opLog.params = "[Serialization Error]";
                }
            }

            // 结果序列化
            if (throwable != null) {
                opLog.result = "Exception: " + throwable.getMessage();
            } else if (result != null) {
                try {
                    opLog.result = objectMapper.writeValueAsString(result);
                } catch (Exception e) {
                    opLog.result = "[Serialization Error]";
                }
            }

            operationLogMapper.insert(opLog);
        } catch (Exception e) {
            log.error("Failed to save operation log", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
