package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
public class CatalogController {
    @Resource private CatalogService catalogService;

    /** 获取分类树。 */
    @GetMapping("/api/categories/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        return ApiResponse.success(catalogService.categoryTree());
    }

    /** 分页获取图书。 */
    @GetMapping("/api/books")
    public ApiResponse<Map<String, Object>> books(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                                  @RequestParam(value = "keyword", required = false) String keyword,
                                                  @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        return ApiResponse.success(catalogService.books(categoryId, keyword, pageNo, pageSize, false));
    }

    /** 获取图书详情。 */
    @GetMapping("/api/books/{id}")
    public ApiResponse<Map<String, Object>> book(@PathVariable("id") Long id) {
        return ApiResponse.success(catalogService.book(id, false));
    }
}
