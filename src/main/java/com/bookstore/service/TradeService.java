package com.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookstore.common.BusinessException;
import com.bookstore.dto.request.CartItemAddRequest;
import com.bookstore.dto.request.CartItemUpdateRequest;
import com.bookstore.dto.request.CreateOrderRequest;
import com.bookstore.mapper.*;
import com.bookstore.model.entity.*;
import com.bookstore.util.AppUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TradeService {
    public static final String CREATED = "CREATED";
    public static final String PAID = "PAID";
    public static final String CANCELLED = "CANCELLED";
    public static final String CONFIRMED = "CONFIRMED";

    @Resource private CartItemMapper cartItemMapper;
    @Resource private BookMapper bookMapper;
    @Resource private BookOrderMapper orderMapper;
    @Resource private OrderItemMapper orderItemMapper;
    @Resource private PaymentRecordMapper paymentRecordMapper;
    @Resource private UserMapper userMapper;
    @Resource private AuthService authService;
    @Resource private CatalogService catalogService;
    @Resource private ObjectMapper objectMapper;

    public List<Map<String, Object>> cartItems() {
        User user = authService.currentUser();
        List<CartItem> list = cartItemMapper.selectList(
                new QueryWrapper<CartItem>().eq("user_id", user.getId()).orderByDesc("updated_time"));
        List<Map<String, Object>> res = new ArrayList<>();
        for (CartItem item : list) {
            Book book = catalogService.bookEntity(item.getBookId(), false);
            Map<String, Object> m = new HashMap<>();
            m.put("id", item.getId());
            m.put("quantity", item.getQuantity());
            m.put("selected", item.getSelected());
            m.put("subtotal", book.getPrice().multiply(new BigDecimal(item.getQuantity())));
            Map<String, Object> bm = new HashMap<>();
            bm.put("id", book.getId());
            bm.put("name", book.getName());
            bm.put("author", book.getAuthor());
            bm.put("coverUrl", book.getCoverUrl());
            bm.put("price", book.getPrice());
            bm.put("stock", book.getStock());
            m.put("book", bm);
            res.add(m);
        }
        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    public CartItem addCartItem(CartItemAddRequest req) {
        User user = authService.currentUser();
        Book book = catalogService.bookEntity(req.getBookId(), false);
        if (book.getStock() < req.getQuantity()) throw new BusinessException(400, "库存不足");
        CartItem item = cartItemMapper.selectOne(
                new QueryWrapper<CartItem>().eq("user_id", user.getId()).eq("book_id", req.getBookId()).last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (item == null) {
            item = new CartItem()
                    .setId(AppUtils.nextId())
                    .setUserId(user.getId())
                    .setBookId(req.getBookId())
                    .setQuantity(req.getQuantity())
                    .setSelected(1)
                    .setCreatedTime(now)
                    .setUpdatedTime(now);
            cartItemMapper.insert(item);
        } else {
            item.setQuantity(item.getQuantity() + req.getQuantity());
            if (item.getQuantity() > book.getStock()) throw new BusinessException(400, "购物车数量超过库存");
            item.setUpdatedTime(now);
            cartItemMapper.updateById(item);
        }
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public CartItem updateCartItem(CartItemUpdateRequest req) {
        User user = authService.currentUser();
        CartItem item = cartItemMapper.selectById(req.getId());
        if (item == null || !user.getId().equals(item.getUserId())) throw new BusinessException(404, "购物车项不存在");
        Book book = catalogService.bookEntity(item.getBookId(), false);
        if (book.getStock() < req.getQuantity()) throw new BusinessException(400, "库存不足");
        item.setQuantity(req.getQuantity());
        item.setUpdatedTime(LocalDateTime.now());
        cartItemMapper.updateById(item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItem(Long id) {
        User user = authService.currentUser();
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !user.getId().equals(item.getUserId())) throw new BusinessException(404, "购物车项不存在");
        cartItemMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(CreateOrderRequest req) {
        User user = authService.currentUser();
        List<CartItem> cart = cartItemMapper.selectList(
                new QueryWrapper<CartItem>().eq("user_id", user.getId()).eq("selected", 1).orderByAsc("created_time"));
        if (cart.isEmpty()) throw new BusinessException(400, "购物车为空");
        LocalDateTime now = LocalDateTime.now();
        BookOrder order = new BookOrder()
                .setId(AppUtils.nextId())
                .setOrderNo(AppUtils.nextOrderNo())
                .setUserId(user.getId())
                .setStatus(CREATED)
                .setReceiverName(req.getReceiverName())
                .setReceiverPhone(req.getReceiverPhone())
                .setReceiverAddress(req.getReceiverAddress())
                .setRemark(req.getRemark())
                .setCreatedTime(now)
                .setUpdatedTime(now);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            Book book = catalogService.bookEntity(item.getBookId(), false);
            if (book.getStock() < item.getQuantity()) throw new BusinessException(400, "库存不足:" + book.getName());
            OrderItem oi = new OrderItem()
                    .setId(AppUtils.nextId())
                    .setOrderId(order.getId())
                    .setBookId(book.getId())
                    .setBookName(book.getName())
                    .setBookAuthor(book.getAuthor())
                    .setCoverUrl(book.getCoverUrl())
                    .setQuantity(item.getQuantity())
                    .setPrice(book.getPrice())
                    .setAmount(book.getPrice().multiply(new BigDecimal(item.getQuantity())));
            total = total.add(oi.getAmount());
            orderItemMapper.insert(oi);
            book.setStock(book.getStock() - item.getQuantity());
            book.setSales((book.getSales() == null ? 0 : book.getSales()) + item.getQuantity());
            book.setUpdatedTime(now);
            bookMapper.updateById(book);
        }
        order.setTotalAmount(total);
        orderMapper.insert(order);
        for (CartItem item : cart) cartItemMapper.deleteById(item.getId());
        return orderDetail(order.getId(), false);
    }

    public List<Map<String, Object>> orders() {
        User user = authService.currentUser();
        return buildOrders(orderMapper.selectList(
                new QueryWrapper<BookOrder>().eq("user_id", user.getId()).orderByDesc("created_time")), false);
    }

    public Map<String, Object> orderDetail(Long id, boolean admin) {
        BookOrder o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "订单不存在");
        if (!admin && !authService.currentUser().getId().equals(o.getUserId()))
            throw new BusinessException(403, "无权查看该订单");
        return buildOrders(Collections.singletonList(o), true).get(0);
    }

    public List<Map<String, Object>> adminOrders(String status, String keyword) {
        QueryWrapper<BookOrder> qw = new QueryWrapper<BookOrder>().orderByDesc("created_time");
        if (StringUtils.hasText(status)) qw.eq("status", status);
        if (StringUtils.hasText(keyword)) qw.and(w -> w.like("order_no", keyword).or().like("receiver_name", keyword));
        return buildOrders(orderMapper.selectList(qw), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> adminUpdateOrderStatus(Long id, String status) {
        if (id == null || !StringUtils.hasText(status)) throw new BusinessException(400, "订单标识和状态不能为空");
        BookOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!CREATED.equals(status) && !PAID.equals(status) && !CANCELLED.equals(status) && !CONFIRMED.equals(status)) {
            throw new BusinessException(400, "不支持的订单状态");
        }
        order.setStatus(status);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return orderDetail(id, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancel(Long id) {
        BookOrder o = ownerOrder(id);
        if (!CREATED.equals(o.getStatus())) throw new BusinessException(400, "当前状态不可取消");
        o.setStatus(CANCELLED);
        o.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateById(o);
        return orderDetail(id, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirm(Long id) {
        BookOrder o = ownerOrder(id);
        if (!PAID.equals(o.getStatus())) throw new BusinessException(400, "只有已支付订单可确认");
        o.setStatus(CONFIRMED);
        o.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateById(o);
        return orderDetail(id, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> prepare(Map<String, Object> body) {
        Long orderId = AppUtils.lng(body, "orderId");
        String payChannel = AppUtils.str(body, "payChannel");
        BookOrder order = ownerOrder(orderId);
        if (!CREATED.equals(order.getStatus())) throw new BusinessException(400, "订单状态不允许支付");
        PaymentRecord p = paymentRecordMapper.selectOne(
                new QueryWrapper<PaymentRecord>().eq("order_id", orderId).last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (p == null) {
            p = new PaymentRecord()
                    .setId(AppUtils.nextId())
                    .setOrderId(orderId)
                    .setCreatedTime(now);
        }
        p.setPayChannel(StringUtils.hasText(payChannel) ? payChannel : "MOCK");
        p.setPayStatus("PREPARED");
        p.setAmount(order.getTotalAmount());
        p.setUpdatedTime(now);
        if (paymentRecordMapper.selectById(p.getId()) == null) paymentRecordMapper.insert(p);
        else paymentRecordMapper.updateById(p);
        Map<String, Object> m = new HashMap<>();
        m.put("paymentId", p.getId());
        m.put("orderId", order.getId());
        m.put("orderNo", order.getOrderNo());
        m.put("amount", p.getAmount());
        m.put("payChannel", p.getPayChannel());
        m.put("mockPayUrl", "/mock/pay?orderNo=" + order.getOrderNo());
        m.put("callbackUrl", "/api/payments/callback");
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> callback(Map<String, Object> body) {
        Long orderId = AppUtils.lng(body, "orderId");
        BookOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        PaymentRecord p = paymentRecordMapper.selectOne(
                new QueryWrapper<PaymentRecord>().eq("order_id", orderId).last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (p == null) {
            p = new PaymentRecord()
                    .setId(AppUtils.nextId())
                    .setOrderId(orderId)
                    .setCreatedTime(now);
        }
        p.setPayChannel(StringUtils.hasText(AppUtils.str(body, "payChannel")) ? AppUtils.str(body, "payChannel") : "MOCK");
        p.setPayStatus("SUCCESS");
        p.setTransactionNo(StringUtils.hasText(AppUtils.str(body, "transactionNo")) ?
                AppUtils.str(body, "transactionNo") : "MOCK-" + AppUtils.nextOrderNo());
        p.setAmount(order.getTotalAmount());
        p.setCallbackContent(json(body));
        p.setPaidTime(now);
        p.setUpdatedTime(now);
        if (paymentRecordMapper.selectById(p.getId()) == null) paymentRecordMapper.insert(p);
        else paymentRecordMapper.updateById(p);
        order.setStatus(PAID);
        order.setUpdatedTime(now);
        orderMapper.updateById(order);
        Map<String, Object> m = new HashMap<>();
        m.put("orderId", order.getId());
        m.put("orderNo", order.getOrderNo());
        m.put("status", order.getStatus());
        m.put("transactionNo", p.getTransactionNo());
        return m;
    }

    private BookOrder ownerOrder(Long id) {
        BookOrder o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "订单不存在");
        if (!authService.currentUser().getId().equals(o.getUserId())) throw new BusinessException(403, "无权操作该订单");
        return o;
    }

    private List<Map<String, Object>> buildOrders(List<BookOrder> orders, boolean includeUser) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BookOrder o : orders) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("status", o.getStatus());
            m.put("totalAmount", o.getTotalAmount());
            m.put("receiverName", o.getReceiverName());
            m.put("receiverPhone", o.getReceiverPhone());
            m.put("receiverAddress", o.getReceiverAddress());
            m.put("remark", o.getRemark());
            m.put("createdTime", o.getCreatedTime());
            m.put("updatedTime", o.getUpdatedTime());
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", o.getId()).orderByAsc("id"));
            List<Map<String, Object>> im = new ArrayList<>();
            for (OrderItem i : items) {
                Map<String, Object> x = new HashMap<>();
                x.put("id", i.getId());
                x.put("bookId", i.getBookId());
                x.put("bookName", i.getBookName());
                x.put("bookAuthor", i.getBookAuthor());
                x.put("coverUrl", i.getCoverUrl());
                x.put("quantity", i.getQuantity());
                x.put("price", i.getPrice());
                x.put("amount", i.getAmount());
                im.add(x);
            }
            m.put("items", im);
            if (includeUser) {
                User u = userMapper.selectById(o.getUserId());
                if (u != null) {
                    Map<String, Object> um = new HashMap<>();
                    um.put("id", u.getId());
                    um.put("username", u.getUsername());
                    um.put("displayName", u.getDisplayName());
                    um.put("phone", u.getPhone());
                    m.put("user", um);
                }
            }
            list.add(m);
        }
        return list;
    }

    private String json(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            return String.valueOf(body);
        }
    }
}
