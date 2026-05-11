package com.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookstore.common.BusinessException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeService {
    public static final String CREATED = "CREATED";
    public static final String PAID = "PAID";
    public static final String CANCELLED = "CANCELLED";
    public static final String CONFIRMED = "CONFIRMED";

    @Resource
    private CartItemMapper cartItemMapper;
    @Resource
    private BookMapper bookMapper;
    @Resource
    private BookOrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AuthService authService;
    @Resource
    private CatalogService catalogService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 当前用户购物车。
     */
    public List<Map<String, Object>> cartItems() {
        User user = authService.currentUser();
        List<CartItem> list = cartItemMapper.selectList(new QueryWrapper<CartItem>().eq("user_id", user.id).orderByDesc("updated_time"));
        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        for (CartItem item : list) {
            Book book = catalogService.bookEntity(item.bookId, false);
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("id", item.id);
            m.put("quantity", item.quantity);
            m.put("selected", item.selected);
            m.put("subtotal", book.price.multiply(new BigDecimal(item.quantity)));
            Map<String, Object> bm = new HashMap<String, Object>();
            bm.put("id", book.id);
            bm.put("name", book.name);
            bm.put("author", book.author);
            bm.put("coverUrl", book.coverUrl);
            bm.put("price", book.price);
            bm.put("stock", book.stock);
            m.put("book", bm);
            res.add(m);
        }
        return res;
    }

    /**
     * 新增购物车商品。
     */
    @Transactional(rollbackFor = Exception.class)
    public CartItem addCartItem(Map<String, Object> body) {
        User user = authService.currentUser();
        Long bookId = AppUtils.lng(body, "bookId");
        Integer quantity = AppUtils.integer(body, "quantity");
        if (bookId == null || quantity == null || quantity < 1)
            throw new BusinessException(400, "bookId 和 quantity 必填");
        Book book = catalogService.bookEntity(bookId, false);
        if (book.stock < quantity) throw new BusinessException(400, "库存不足");
        CartItem item = cartItemMapper.selectOne(new QueryWrapper<CartItem>().eq("user_id", user.id).eq("book_id", bookId).last("limit 1"));
        if (item == null) {
            item = new CartItem();
            item.id = AppUtils.nextId();
            item.userId = user.id;
            item.bookId = bookId;
            item.quantity = quantity;
            item.selected = 1;
            item.createdTime = LocalDateTime.now();
            item.updatedTime = LocalDateTime.now();
            cartItemMapper.insert(item);
        } else {
            item.quantity = item.quantity + quantity;
            if (item.quantity > book.stock) throw new BusinessException(400, "购物车数量超过库存");
            item.updatedTime = LocalDateTime.now();
            cartItemMapper.updateById(item);
        }
        return item;
    }

    /**
     * 修改购物车商品数量。
     */
    @Transactional(rollbackFor = Exception.class)
    public CartItem updateCartItem(Map<String, Object> body) {
        Long id = AppUtils.lng(body, "id");
        Integer quantity = AppUtils.integer(body, "quantity");
        if (id == null || quantity == null || quantity < 1) throw new BusinessException(400, "id 和 quantity 必填");
        User user = authService.currentUser();
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !user.id.equals(item.userId)) throw new BusinessException(404, "购物车项不存在");
        Book book = catalogService.bookEntity(item.bookId, false);
        if (book.stock < quantity) throw new BusinessException(400, "库存不足");
        item.quantity = quantity;
        item.updatedTime = LocalDateTime.now();
        cartItemMapper.updateById(item);
        return item;
    }

    /**
     * 删除购物车项。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItem(Long id) {
        User user = authService.currentUser();
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !user.id.equals(item.userId)) throw new BusinessException(404, "购物车项不存在");
        cartItemMapper.deleteById(id);
    }

    /**
     * 创建订单。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Map<String, Object> body) {
        User user = authService.currentUser();
        List<CartItem> cart = cartItemMapper.selectList(new QueryWrapper<CartItem>().eq("user_id", user.id).eq("selected", 1).orderByAsc("created_time"));
        if (cart.isEmpty()) throw new BusinessException(400, "购物车为空");
        String receiverName = AppUtils.str(body, "receiverName");
        String receiverPhone = AppUtils.str(body, "receiverPhone");
        String receiverAddress = AppUtils.str(body, "receiverAddress");
        if (!StringUtils.hasText(receiverName) || !StringUtils.hasText(receiverPhone) || !StringUtils.hasText(receiverAddress))
            throw new BusinessException(400, "收货信息不能为空");
        LocalDateTime now = LocalDateTime.now();
        BookOrder order = new BookOrder();
        order.id = AppUtils.nextId();
        order.orderNo = AppUtils.nextOrderNo();
        order.userId = user.id;
        order.status = CREATED;
        order.receiverName = receiverName;
        order.receiverPhone = receiverPhone;
        order.receiverAddress = receiverAddress;
        order.remark = AppUtils.str(body, "remark");
        order.createdTime = now;
        order.updatedTime = now;
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            Book book = catalogService.bookEntity(item.bookId, false);
            if (book.stock < item.quantity) throw new BusinessException(400, "库存不足:" + book.name);
            OrderItem oi = new OrderItem();
            oi.id = AppUtils.nextId();
            oi.orderId = order.id;
            oi.bookId = book.id;
            oi.bookName = book.name;
            oi.bookAuthor = book.author;
            oi.coverUrl = book.coverUrl;
            oi.quantity = item.quantity;
            oi.price = book.price;
            oi.amount = book.price.multiply(new BigDecimal(item.quantity));
            total = total.add(oi.amount);
            orderItemMapper.insert(oi);
            book.stock = book.stock - item.quantity;
            book.sales = (book.sales == null ? 0 : book.sales) + item.quantity;
            book.updatedTime = now;
            bookMapper.updateById(book);
        }
        order.totalAmount = total;
        orderMapper.insert(order);
        for (CartItem item : cart) cartItemMapper.deleteById(item.id);
        return orderDetail(order.id, false);
    }

    /**
     * 当前用户订单列表。
     */
    public List<Map<String, Object>> orders() {
        User user = authService.currentUser();
        return buildOrders(orderMapper.selectList(new QueryWrapper<BookOrder>().eq("user_id", user.id).orderByDesc("created_time")), false);
    }

    /**
     * 获取订单详情，支持当前用户和管理员两种查看视角。
     */
    public Map<String, Object> orderDetail(Long id, boolean admin) {
        BookOrder o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "订单不存在");
        if (!admin && !authService.currentUser().id.equals(o.userId))
            throw new BusinessException(403, "无权查看该订单");
        return buildOrders(java.util.Collections.singletonList(o), true).get(0);
    }

    /**
     * 管理端订单列表。
     */
    public List<Map<String, Object>> adminOrders(String status, String keyword) {
        QueryWrapper<BookOrder> qw = new QueryWrapper<BookOrder>().orderByDesc("created_time");
        if (StringUtils.hasText(status)) qw.eq("status", status);
        if (StringUtils.hasText(keyword)) qw.and(w -> w.like("order_no", keyword).or().like("receiver_name", keyword));
        return buildOrders(orderMapper.selectList(qw), true);
    }

    /**
     * 管理端更新订单状态。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> adminUpdateOrderStatus(Long id, String status) {
        if (id == null || !StringUtils.hasText(status)) throw new BusinessException(400, "订单标识和状态不能为空");
        BookOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!CREATED.equals(status) && !PAID.equals(status) && !CANCELLED.equals(status) && !CONFIRMED.equals(status)) {
            throw new BusinessException(400, "不支持的订单状态");
        }
        order.status = status;
        order.updatedTime = LocalDateTime.now();
        orderMapper.updateById(order);
        return orderDetail(id, true);
    }

    /**
     * 取消订单。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancel(Long id) {
        BookOrder o = ownerOrder(id);
        if (!CREATED.equals(o.status)) throw new BusinessException(400, "当前状态不可取消");
        o.status = CANCELLED;
        o.updatedTime = LocalDateTime.now();
        orderMapper.updateById(o);
        return orderDetail(id, false);
    }

    /**
     * 确认收货。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirm(Long id) {
        BookOrder o = ownerOrder(id);
        if (!PAID.equals(o.status)) throw new BusinessException(400, "只有已支付订单可确认");
        o.status = CONFIRMED;
        o.updatedTime = LocalDateTime.now();
        orderMapper.updateById(o);
        return orderDetail(id, false);
    }

    /**
     * 模拟支付预下单。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> prepare(Map<String, Object> body) {
        Long orderId = AppUtils.lng(body, "orderId");
        String payChannel = AppUtils.str(body, "payChannel");
        BookOrder order = ownerOrder(orderId);
        if (!CREATED.equals(order.status)) throw new BusinessException(400, "订单状态不允许支付");
        PaymentRecord p = paymentRecordMapper.selectOne(new QueryWrapper<PaymentRecord>().eq("order_id", orderId).last("limit 1"));
        if (p == null) {
            p = new PaymentRecord();
            p.id = AppUtils.nextId();
            p.orderId = orderId;
            p.createdTime = LocalDateTime.now();
        }
        p.payChannel = StringUtils.hasText(payChannel) ? payChannel : "MOCK";
        p.payStatus = "PREPARED";
        p.amount = order.totalAmount;
        p.updatedTime = LocalDateTime.now();
        if (paymentRecordMapper.selectById(p.id) == null) paymentRecordMapper.insert(p);
        else paymentRecordMapper.updateById(p);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("paymentId", p.id);
        m.put("orderId", order.id);
        m.put("orderNo", order.orderNo);
        m.put("amount", p.amount);
        m.put("payChannel", p.payChannel);
        m.put("mockPayUrl", "/mock/pay?orderNo=" + order.orderNo);
        m.put("callbackUrl", "/api/payments/callback");
        return m;
    }

    /**
     * 模拟支付回调。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> callback(Map<String, Object> body) {
        Long orderId = AppUtils.lng(body, "orderId");
        BookOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        PaymentRecord p = paymentRecordMapper.selectOne(new QueryWrapper<PaymentRecord>().eq("order_id", orderId).last("limit 1"));
        if (p == null) {
            p = new PaymentRecord();
            p.id = AppUtils.nextId();
            p.orderId = orderId;
            p.createdTime = LocalDateTime.now();
        }
        p.payChannel = StringUtils.hasText(AppUtils.str(body, "payChannel")) ? AppUtils.str(body, "payChannel") : "MOCK";
        p.payStatus = "SUCCESS";
        p.transactionNo = StringUtils.hasText(AppUtils.str(body, "transactionNo")) ? AppUtils.str(body, "transactionNo") : "MOCK-" + AppUtils.nextOrderNo();
        p.amount = order.totalAmount;
        p.callbackContent = json(body);
        p.paidTime = LocalDateTime.now();
        p.updatedTime = LocalDateTime.now();
        if (paymentRecordMapper.selectById(p.id) == null) paymentRecordMapper.insert(p);
        else paymentRecordMapper.updateById(p);
        order.status = PAID;
        order.updatedTime = LocalDateTime.now();
        orderMapper.updateById(order);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("orderId", order.id);
        m.put("orderNo", order.orderNo);
        m.put("status", order.status);
        m.put("transactionNo", p.transactionNo);
        return m;
    }

    private BookOrder ownerOrder(Long id) {
        BookOrder o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "订单不存在");
        if (!authService.currentUser().id.equals(o.userId)) throw new BusinessException(403, "无权操作该订单");
        return o;
    }

    private List<Map<String, Object>> buildOrders(List<BookOrder> orders, boolean includeUser) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (BookOrder o : orders) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("id", o.id);
            m.put("orderNo", o.orderNo);
            m.put("status", o.status);
            m.put("totalAmount", o.totalAmount);
            m.put("receiverName", o.receiverName);
            m.put("receiverPhone", o.receiverPhone);
            m.put("receiverAddress", o.receiverAddress);
            m.put("remark", o.remark);
            m.put("createdTime", o.createdTime);
            m.put("updatedTime", o.updatedTime);
            List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", o.id).orderByAsc("id"));
            List<Map<String, Object>> im = new ArrayList<Map<String, Object>>();
            for (OrderItem i : items) {
                Map<String, Object> x = new HashMap<String, Object>();
                x.put("id", i.id);
                x.put("bookId", i.bookId);
                x.put("bookName", i.bookName);
                x.put("bookAuthor", i.bookAuthor);
                x.put("coverUrl", i.coverUrl);
                x.put("quantity", i.quantity);
                x.put("price", i.price);
                x.put("amount", i.amount);
                im.add(x);
            }
            m.put("items", im);
            if (includeUser) {
                User u = userMapper.selectById(o.userId);
                if (u != null) {
                    Map<String, Object> um = new HashMap<String, Object>();
                    um.put("id", u.id);
                    um.put("username", u.username);
                    um.put("displayName", u.displayName);
                    um.put("phone", u.phone);
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
