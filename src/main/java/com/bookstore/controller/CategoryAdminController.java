package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.dto.request.CategorySaveRequest;
import com.bookstore.model.entity.Category;
import com.bookstore.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryAdminController {
    @Resource private CatalogService catalogService;

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(catalogService.adminCategories());
    }

    @Log("新增分类")
    @PostMapping
    public ApiResponse<Category> add(@Valid @RequestBody CategorySaveRequest req) {
        return ApiResponse.success("保存成功", catalogService.saveCategory(req));
    }

    @Log("更新分类")
    @PutMapping
    public ApiResponse<Category> update(@Valid @RequestBody CategorySaveRequest req) {
        return ApiResponse.success("保存成功", catalogService.saveCategory(req));
    }

    @Log("删除分类")
    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam("id") Long id) {
        catalogService.deleteCategory(id);
        return ApiResponse.success("删除成功", null);
    }
}
