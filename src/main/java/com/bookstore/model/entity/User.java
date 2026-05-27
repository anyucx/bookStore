package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("users")
public class User {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String username;
    @TableField("password_hash")
    private String passwordHash;
    @TableField("display_name")
    private String displayName;
    private String phone;
    private String email;
    @TableField("role_id")
    private Long roleId;
    private Integer status;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
