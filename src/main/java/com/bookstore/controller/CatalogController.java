package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
public class CatalogController {
    @Resource private CatalogService catalogService;

    @GetMapping("/api/categories/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        return ApiResponse.success(catalogService.categoryTree());
    }

    @GetMapping("/api/books")
    public ApiResponse<Map<String, Object>> books(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                                   @RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        return ApiResponse.success(catalogService.books(categoryId, keyword, pageNo, pageSize, false));
    }

    @GetMapping("/api/books/{id}")
    public ApiResponse<Map<String, Object>> book(@PathVariable("id") Long id) {
        return ApiResponse.success(catalogService.book(id, false));
    }
}
