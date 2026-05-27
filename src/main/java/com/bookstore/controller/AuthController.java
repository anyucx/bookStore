package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.dto.request.LoginRequest;
import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
public class AuthController {
    @Resource private AuthService authService;

    @Log("用户注册")
    @PostMapping("/api/auth/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.success("注册成功", authService.register(req));
    }

    @Log("用户登录")
    @PostMapping("/api/auth/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.success("登录成功", authService.login(req, false));
    }

    @Log("管理员登录")
    @PostMapping("/api/admin/auth/login")
    public ApiResponse<Map<String, Object>> adminLogin(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.success("登录成功", authService.login(req, true));
    }

    @Log("用户登出")
    @PostMapping("/api/auth/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) token = authorization.substring(7).trim();
        authService.logout(token);
        return ApiResponse.success("退出成功", null);
    }

    @GetMapping("/api/auth/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.success(authService.me());
    }
}
