package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("books")
public class Book {
    @TableId(type = IdType.INPUT) public Long id;
    @TableField("category_id") public Long categoryId;
    public String name;
    public String author;
    public String isbn;
    public BigDecimal price;
    public Integer stock;
    @TableField("cover_url") public String coverUrl;
    public String description;
    public Integer status;
    public Integer sales;
    @TableField("created_time") public LocalDateTime createdTime;
    @TableField("updated_time") public LocalDateTime updatedTime;
}
