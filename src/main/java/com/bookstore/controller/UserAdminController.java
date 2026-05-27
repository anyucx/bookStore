package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.dto.request.UserUpdateRequest;
import com.bookstore.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {
    @Resource private AuthService authService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.success(authService.users(keyword, status));
    }

    @Log("更新用户信息")
    @PutMapping
    public ApiResponse<Map<String, Object>> update(@Valid @RequestBody UserUpdateRequest req) {
        return ApiResponse.success("更新成功", authService.updateUser(req));
    }
}
