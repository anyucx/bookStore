package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("categories")
public class Category {
    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("parent_id")
    private Long parentId;
    private String name;
    private Integer sort;
    private Integer status;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
