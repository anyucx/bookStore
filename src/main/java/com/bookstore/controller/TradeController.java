package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.dto.request.CartItemAddRequest;
import com.bookstore.dto.request.CartItemUpdateRequest;
import com.bookstore.dto.request.CreateOrderRequest;
import com.bookstore.model.entity.CartItem;
import com.bookstore.service.TradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class TradeController {
    @Resource private TradeService tradeService;

    @GetMapping("/api/cart/items")
    public ApiResponse<List<Map<String, Object>>> cartItems() {
        return ApiResponse.success(tradeService.cartItems());
    }

    @Log("添加购物车")
    @PostMapping("/api/cart/items")
    public ApiResponse<CartItem> addCartItem(@Valid @RequestBody CartItemAddRequest req) {
        return ApiResponse.success("加入购物车成功", tradeService.addCartItem(req));
    }

    @Log("更新购物车")
    @PutMapping("/api/cart/items")
    public ApiResponse<CartItem> updateCartItem(@Valid @RequestBody CartItemUpdateRequest req) {
        return ApiResponse.success("更新成功", tradeService.updateCartItem(req));
    }

    @Log("删除购物车商品")
    @DeleteMapping("/api/cart/items")
    public ApiResponse<Void> deleteCartItem(@RequestParam("id") Long id) {
        tradeService.deleteCartItem(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/api/orders")
    public ApiResponse<List<Map<String, Object>>> orders() {
        return ApiResponse.success(tradeService.orders());
    }

    @GetMapping("/api/orders/{id}")
    public ApiResponse<Map<String, Object>> orderDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(tradeService.orderDetail(id, false));
    }

    @Log("创建订单")
    @PostMapping("/api/orders")
    public ApiResponse<Map<String, Object>> createOrder(@Valid @RequestBody CreateOrderRequest req) {
        return ApiResponse.success("下单成功", tradeService.createOrder(req));
    }

    @Log("取消订单")
    @PostMapping("/api/orders/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable("id") Long id) {
        return ApiResponse.success("取消成功", tradeService.cancel(id));
    }

    @Log("确认收货")
    @PostMapping("/api/orders/{id}/confirm")
    public ApiResponse<Map<String, Object>> confirm(@PathVariable("id") Long id) {
        return ApiResponse.success("确认收货成功", tradeService.confirm(id));
    }

    @Log("支付预下单")
    @PostMapping("/api/payments/prepare")
    public ApiResponse<Map<String, Object>> prepare(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(tradeService.prepare(body));
    }

    @Log("支付回调")
    @PostMapping("/api/payments/callback")
    public ApiResponse<Map<String, Object>> callback(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(tradeService.callback(body));
    }
}
