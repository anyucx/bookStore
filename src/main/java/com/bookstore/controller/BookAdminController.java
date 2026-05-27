package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.dto.request.BookSaveRequest;
import com.bookstore.model.entity.Book;
import com.bookstore.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/books")
public class BookAdminController {
    @Resource private CatalogService catalogService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                                  @RequestParam(value = "keyword", required = false) String keyword,
                                                  @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        return ApiResponse.success(catalogService.books(categoryId, keyword, pageNo, pageSize, true));
    }

    @Log("新增图书")
    @PostMapping
    public ApiResponse<Book> add(@Valid @RequestBody BookSaveRequest req) {
        return ApiResponse.success("保存成功", catalogService.saveBook(req));
    }

    @Log("更新图书")
    @PutMapping
    public ApiResponse<Book> update(@Valid @RequestBody BookSaveRequest req) {
        return ApiResponse.success("保存成功", catalogService.saveBook(req));
    }

    @Log("删除图书")
    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam("id") Long id) {
        catalogService.deleteBook(id);
        return ApiResponse.success("删除成功", null);
    }
}
