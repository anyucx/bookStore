package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.dto.request.AdminOrderUpdateRequest;
import com.bookstore.service.TradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminController {
    @Resource private TradeService tradeService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(value = "status", required = false) String status,
                                                        @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.success(tradeService.adminOrders(status, keyword));
    }

    @Log("更新订单状态")
    @PutMapping
    public ApiResponse<Map<String, Object>> updateStatus(@Valid @RequestBody AdminOrderUpdateRequest req) {
        return ApiResponse.success("更新成功", tradeService.adminUpdateOrderStatus(req.getId(), req.getStatus()));
    }
}
