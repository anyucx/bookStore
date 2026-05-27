package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("cart_items")
public class CartItem {
    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("book_id")
    private Long bookId;
    private Integer quantity;
    private Integer selected;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
