package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import com.bookstore.common.annotation.Log;
import com.bookstore.model.entity.CartItem;
import com.bookstore.service.TradeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
public class TradeController {
    @Resource private TradeService tradeService;

    /** 获取购物车列表。 */
    @GetMapping("/api/cart/items")
    public ApiResponse<List<Map<String, Object>>> cartItems() {
        return ApiResponse.success(tradeService.cartItems());
    }

    /** 新增购物车商品。 */
    @Log("添加购物车")
    @PostMapping("/api/cart/items")
    public ApiResponse<CartItem> addCartItem(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("加入购物车成功", tradeService.addCartItem(body));
    }

    /** 更新购物车商品。 */
    @Log("更新购物车")
    @PutMapping("/api/cart/items")
    public ApiResponse<CartItem> updateCartItem(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("更新成功", tradeService.updateCartItem(body));
    }

    /** 删除购物车商品。 */
    @Log("删除购物车商品")
    @DeleteMapping("/api/cart/items")
    public ApiResponse<Void> deleteCartItem(@RequestParam("id") Long id) {
        tradeService.deleteCartItem(id);
        return ApiResponse.success("删除成功", null);
    }

    /** 获取当前用户订单。 */
    @GetMapping("/api/orders")
    public ApiResponse<List<Map<String, Object>>> orders() {
        return ApiResponse.success(tradeService.orders());
    }

    /** 获取当前用户订单详情。 */
    @GetMapping("/api/orders/{id}")
    public ApiResponse<Map<String, Object>> orderDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(tradeService.orderDetail(id, false));
    }

    /** 创建订单。 */
    @Log("创建订单")
    @PostMapping("/api/orders")
    public ApiResponse<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body) {
        return ApiResponse.success("下单成功", tradeService.createOrder(body));
    }

    /** 取消订单。 */
    @Log("取消订单")
    @PostMapping("/api/orders/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable("id") Long id) {
        return ApiResponse.success("取消成功", tradeService.cancel(id));
    }

    /** 确认收货。 */
    @Log("确认收货")
    @PostMapping("/api/orders/{id}/confirm")
    public ApiResponse<Map<String, Object>> confirm(@PathVariable("id") Long id) {
        return ApiResponse.success("确认收货成功", tradeService.confirm(id));
    }

    /** 支付预下单。 */
    @Log("支付预下单")
    @PostMapping("/api/payments/prepare")
    public ApiResponse<Map<String, Object>> prepare(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(tradeService.prepare(body));
    }

    /** 支付回调。 */
    @Log("支付回调")
    @PostMapping("/api/payments/callback")
    public ApiResponse<Map<String, Object>> callback(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(tradeService.callback(body));
    }
}
