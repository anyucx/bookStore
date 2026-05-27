package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("books")
public class Book {
    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("category_id")
    private Long categoryId;
    private String name;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Integer stock;
    @TableField("cover_url")
    private String coverUrl;
    private String description;
    private Integer status;
    private Integer sales;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
