package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

@TableName("categories")
public class Category {
    @TableId(type = IdType.INPUT) public Long id;
    @TableField("parent_id") public Long parentId;
    public String name;
    public Integer sort;
    public Integer status;
    @TableField("created_time") public LocalDateTime createdTime;
    @TableField("updated_time") public LocalDateTime updatedTime;
}
