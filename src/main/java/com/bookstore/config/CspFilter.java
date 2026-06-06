package com.bookstore.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class CspFilter {

    @Bean
    public FilterRegistrationBean<Filter> cspHeaderFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter((ServletRequest request, ServletResponse response, FilterChain chain) -> {
            HttpServletResponse res = (HttpServletResponse) response;
            res.setHeader("Content-Security-Policy",
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self' data:; " +
                    "connect-src 'self' ws: wss:; " +
                    "frame-ancestors 'none'; " +
                    "object-src 'none'");
            chain.doFilter(request, response);
        });
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }
}
