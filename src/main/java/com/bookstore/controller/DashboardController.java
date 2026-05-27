package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
public class DashboardController {
    @Resource private AdminService adminService;

    @GetMapping("/api/admin/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.success(adminService.dashboard());
    }
}
