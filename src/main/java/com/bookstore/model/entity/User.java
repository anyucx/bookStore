package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

@TableName("users")
public class User {
    @TableId(type = IdType.INPUT) public Long id;
    public String username;
    @TableField("password_hash") public String passwordHash;
    @TableField("display_name") public String displayName;
    public String phone;
    public String email;
    @TableField("role_id") public Long roleId;
    public Integer status;
    @TableField("created_time") public LocalDateTime createdTime;
    @TableField("updated_time") public LocalDateTime updatedTime;
}
