package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.service.AdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@RestController
public class FileUploadController {
    @Resource private AdminService adminService;

    @Log("上传文件")
    @PostMapping("/api/admin/files/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.success("上传成功", adminService.upload(file));
    }
}
