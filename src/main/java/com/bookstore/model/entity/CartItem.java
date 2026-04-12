package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

@TableName("cart_items")
public class CartItem {
    @TableId(type = IdType.INPUT) public Long id;
    @TableField("user_id") public Long userId;
    @TableField("book_id") public Long bookId;
    public Integer quantity;
    public Integer selected;
    @TableField("created_time") public LocalDateTime createdTime;
    @TableField("updated_time") public LocalDateTime updatedTime;
}
