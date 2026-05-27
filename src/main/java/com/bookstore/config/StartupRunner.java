package com.bookstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void run(String... args) {
        String baseUrl = "http://localhost:" + port + contextPath;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        String adminLoginUrl = baseUrl + "admin/login";

        log.info("====================================================================");
        log.info("项目启动成功！");
        log.info("商城首页 (Shop): {}", baseUrl);
        log.info("管理后台 (Admin): {}", adminLoginUrl);
        log.info("管理员账号: admin");
        log.info("管理员密码: 123456");
        log.info("====================================================================");
    }
}
