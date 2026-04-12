package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

@TableName("roles")
public class Role {
    @TableId(type = IdType.INPUT) public Long id;
    public String name;
    public String code;
    public String description;
    @TableField("created_time") public LocalDateTime createdTime;
}
