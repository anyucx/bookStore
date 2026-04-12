package com.bookstore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@SpringBootApplication
@MapperScan("com.bookstore.mapper")
public class BookstoreApplication {
    public static void main(String[] args) {
        buildFrontend();
        SpringApplication.run(BookstoreApplication.class, args);
    }

    private static void buildFrontend() {
        System.out.println(">>> 正在构建前端包，确保显示最新内容...");
        try {
            File frontendDir = new File("frontend");
            if (!frontendDir.exists() || !frontendDir.isDirectory()) {
                System.err.println(">>> 找不到前端目录: " + frontendDir.getAbsolutePath());
                return;
            }

            String os = System.getProperty("os.name").toLowerCase();
            String npmCommand = os.contains("win") ? "npm.cmd" : "npm";

            ProcessBuilder processBuilder = new ProcessBuilder(npmCommand, "run", "build");
            processBuilder.directory(frontendDir);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Frontend Build] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(">>> 前端包构建成功！");
            } else {
                System.err.println(">>> 前端包构建失败，退出码：" + exitCode);
            }
        } catch (Exception e) {
            System.err.println(">>> 构建前端包时发生错误: " + e.getMessage());
            // 继续启动应用，避免因前端构建失败导致整个后端无法启动
        }
    }
}
