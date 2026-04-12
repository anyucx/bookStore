package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;

@TableName("order_items")
public class OrderItem {
    @TableId(type = IdType.INPUT) public Long id;
    @TableField("order_id") public Long orderId;
    @TableField("book_id") public Long bookId;
    @TableField("book_name") public String bookName;
    @TableField("book_author") public String bookAuthor;
    @TableField("cover_url") public String coverUrl;
    public Integer quantity;
    public BigDecimal price;
    public BigDecimal amount;
}
