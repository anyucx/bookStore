package com.bookstore.security;

import com.bookstore.common.BusinessException;
import com.bookstore.util.RedisCacheUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private ObjectMapper objectMapper;
    @Value("${bookstore.auth.token-prefix:bookstore:token:}")
    private String tokenPrefix;
    @Value("${bookstore.auth.token-ttl-days:7}")
    private long tokenTtlDays;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (isPublic(request.getMethod(), uri)) {
            return true;
        }
        String token = token(request);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(401, "未登录或 token 缺失");
        }
        Object cache = redisCacheUtil.getCacheObject(tokenPrefix + token);
        if (cache == null) {
            throw new BusinessException(401, "登录态已失效");
        }
        SecuritySupport.LoginUser user = objectMapper.convertValue(cache, SecuritySupport.LoginUser.class);
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(401, "登录态无效");
        }
        if (uri.startsWith("/api/admin/") && !"/api/admin/auth/login".equals(uri) && !"ADMIN".equalsIgnoreCase(user.getRoleCode())) {
            throw new BusinessException(403, "无管理员权限");
        }
        SecuritySupport.set(user);
        redisCacheUtil.expire(tokenPrefix + token, tokenTtlDays, TimeUnit.DAYS);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecuritySupport.clear();
    }

    private boolean isPublic(String method, String uri) {
        return ("POST".equalsIgnoreCase(method) && ("/api/auth/register".equals(uri) || "/api/auth/login".equals(uri)
                || "/api/admin/auth/login".equals(uri) || "/api/payments/callback".equals(uri)))
                || ("GET".equalsIgnoreCase(method) && ("/api/categories/tree".equals(uri) || "/api/books".equals(uri)
                || uri.startsWith("/api/books/")));
    }

    private String token(HttpServletRequest request) {
        String v = request.getHeader("Authorization");
        if (StringUtils.hasText(v) && v.startsWith("Bearer ")) {
            return v.substring(7).trim();
        }
        v = request.getHeader("X-Token");
        if (StringUtils.hasText(v)) {
            return v.trim();
        }
        return null;
    }
}
