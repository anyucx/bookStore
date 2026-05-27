package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("roles")
public class Role {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private String code;
    private String description;
    @TableField("created_time")
    private LocalDateTime createdTime;
}
