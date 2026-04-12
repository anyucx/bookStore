package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.model.entity.Book;
import com.bookstore.model.entity.Category;
import com.bookstore.service.AdminService;
import com.bookstore.service.AuthService;
import com.bookstore.service.CatalogService;
import com.bookstore.service.TradeService;
import com.bookstore.util.AppUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class AdminController {
    @Resource private AuthService authService;
    @Resource private CatalogService catalogService;
    @Resource private TradeService tradeService;
    @Resource private AdminService adminService;

    /** 管理员登录。 */
    @PostMapping("/api/admin/auth/login")
    public ApiResponse<Map<String, Object>> adminLogin(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("登录成功", authService.login(body, true));
    }

    /** 获取管理端首页统计。 */
    @GetMapping("/api/admin/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.success(adminService.dashboard());
    }

    /** 获取管理端分类列表。 */
    @GetMapping("/api/admin/categories")
    public ApiResponse<List<Category>> adminCategories() {
        return ApiResponse.success(catalogService.adminCategories());
    }

    /** 新增分类。 */
    @PostMapping("/api/admin/categories")
    public ApiResponse<Category> addCategory(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("保存成功", catalogService.saveCategory(body));
    }

    /** 更新分类。 */
    @PutMapping("/api/admin/categories")
    public ApiResponse<Category> updateCategory(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("保存成功", catalogService.saveCategory(body));
    }

    /** 删除分类。 */
    @DeleteMapping("/api/admin/categories")
    public ApiResponse<Void> deleteCategory(@RequestParam("id") Long id) {
        catalogService.deleteCategory(id);
        return ApiResponse.success("删除成功", null);
    }

    /** 获取管理端图书列表。 */
    @GetMapping("/api/admin/books")
    public ApiResponse<Map<String, Object>> adminBooks(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                                       @RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
                                                       @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        return ApiResponse.success(catalogService.books(categoryId, keyword, pageNo, pageSize, true));
    }

    /** 新增图书。 */
    @PostMapping("/api/admin/books")
    public ApiResponse<Book> addBook(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("保存成功", catalogService.saveBook(body));
    }

    /** 更新图书。 */
    @PutMapping("/api/admin/books")
    public ApiResponse<Book> updateBook(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("保存成功", catalogService.saveBook(body));
    }

    /** 删除图书。 */
    @DeleteMapping("/api/admin/books")
    public ApiResponse<Void> deleteBook(@RequestParam("id") Long id) {
        catalogService.deleteBook(id);
        return ApiResponse.success("删除成功", null);
    }

    /** 获取管理端订单列表。 */
    @GetMapping("/api/admin/orders")
    public ApiResponse<List<Map<String, Object>>> adminOrders(@RequestParam(value = "status", required = false) String status,
                                                              @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.success(tradeService.adminOrders(status, keyword));
    }

    /** 更新管理端订单状态。 */
    @PutMapping("/api/admin/orders")
    public ApiResponse<Map<String, Object>> updateOrder(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("更新成功", tradeService.adminUpdateOrderStatus(AppUtils.lng(body, "id"), AppUtils.str(body, "status")));
    }

    /** 获取管理端用户列表。 */
    @GetMapping("/api/admin/users")
    public ApiResponse<List<Map<String, Object>>> users(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.success(authService.users(keyword, status));
    }

    /** 更新管理端用户。 */
    @PutMapping("/api/admin/users")
    public ApiResponse<Map<String, Object>> updateUser(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("更新成功", authService.updateUser(body));
    }

    /** 上传文件。 */
    @PostMapping("/api/admin/files/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.success("上传成功", adminService.upload(file));
    }
}
