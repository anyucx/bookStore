package com.bookstore.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.bookstore.security.AuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Resource
    @Lazy
    private AuthInterceptor authInterceptor;

    @Value("${bookstore.file.base-path:uploads}")
    private String fileBasePath;

    @Value("${bookstore.file.base-url-prefix:/uploads/}")
    private String fileBaseUrlPrefix;

    @Value("${bookstore.frontend.dist-path:frontend/dist}")
    private String frontendDistPath;

    @Value("${bookstore.frontend.index-file:index.html}")
    private String frontendIndexFile;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pattern = fileBaseUrlPrefix.endsWith("/") ? fileBaseUrlPrefix + "**" : fileBaseUrlPrefix + "/**";
        registry.addResourceHandler(pattern).addResourceLocations(Paths.get(fileBasePath).toAbsolutePath().normalize().toUri().toString());
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", resolveFrontendDistLocation())
                .resourceChain(false)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(String resourcePath, org.springframework.core.io.Resource location) throws IOException {
                        org.springframework.core.io.Resource resource = location.createRelative(resourcePath);
                        // 如果资源存在、可读且不是目录，则返回该资源
                        if (resource.exists() && resource.isReadable()) {
                            // 检查是否为目录，如果是目录则应回退到 index.html
                            if (!resource.getURL().getPath().endsWith("/")) {
                                return resource;
                            }
                        }
                        // 否则尝试返回 SPA 的 index.html
                        if (shouldServeSpaIndex(resourcePath)) {
                            org.springframework.core.io.Resource index = location.createRelative(frontendIndexFile);
                            if (index.exists() && index.isReadable()) {
                                return index;
                            }
                        }
                        return null;
                    }
                });
    }

    private String resolveFrontendDistLocation() {
        String location = Paths.get(frontendDistPath).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            return location + "/";
        }
        return location;
    }

    private boolean shouldServeSpaIndex(String resourcePath) {
        if (!StringUtils.hasText(resourcePath)) {
            return true;
        }
        String normalizedPath = trimSlashes(resourcePath);
        if (!StringUtils.hasText(normalizedPath)) {
            return true;
        }
        if (normalizedPath.startsWith("api/")) {
            return false;
        }
        String uploadPrefix = trimSlashes(fileBaseUrlPrefix);
        if (StringUtils.hasText(uploadPrefix) && (normalizedPath.equals(uploadPrefix) || normalizedPath.startsWith(uploadPrefix + "/"))) {
            return false;
        }
        return !normalizedPath.contains(".");
    }

    private String trimSlashes(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        int start = 0;
        int end = path.length();
        while (start < end && path.charAt(start) == '/') {
            start++;
        }
        while (end > start && path.charAt(end - 1) == '/') {
            end--;
        }
        return path.substring(start, end);
    }
}
