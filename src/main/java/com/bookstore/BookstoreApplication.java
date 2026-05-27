package com.bookstore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@MapperScan("com.bookstore.mapper")
public class BookstoreApplication {
    public static void main(String[] args) {
        buildFrontend();
        SpringApplication.run(BookstoreApplication.class, args);
    }

    private static void buildFrontend() {
        System.out.println(">>> Building frontend...");
        try {
            File frontendDir = new File("frontend");
            if (!frontendDir.exists() || !frontendDir.isDirectory()) {
                System.err.println(">>> Frontend directory not found: " + frontendDir.getAbsolutePath());
                return;
            }

            String os = System.getProperty("os.name").toLowerCase();
            String npmCommand = os.contains("win") ? "npm.cmd" : "npm";

            ProcessBuilder processBuilder = new ProcessBuilder(npmCommand, "run", "build");
            processBuilder.directory(frontendDir);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Frontend Build] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(">>> Frontend built successfully!");
            } else {
                System.err.println(">>> Frontend build failed, exit code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println(">>> Frontend build error: " + e.getMessage());
        }
    }
}
