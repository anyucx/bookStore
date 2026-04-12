package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.service.AuthService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Resource private AuthService authService;

    /** 用户注册。 */
    @Log("用户注册")
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("注册成功", authService.register(body));
    }

    /** 用户登录。 */
    @Log("用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("登录成功", authService.login(body, false));
    }

    /** 用户退出登录。 */
    @Log("用户登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = null;
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) token = authorization.substring(7).trim();
        authService.logout(token);
        return ApiResponse.success("退出成功", null);
    }

    /** 获取当前用户信息。 */
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.success(authService.me());
    }
}
