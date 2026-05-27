package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@TableName("order_items")
public class OrderItem {
    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("book_id")
    private Long bookId;
    @TableField("book_name")
    private String bookName;
    @TableField("book_author")
    private String bookAuthor;
    @TableField("cover_url")
    private String coverUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;
}
