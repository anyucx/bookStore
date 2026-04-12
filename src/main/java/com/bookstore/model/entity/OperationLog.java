package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

@TableName("operation_logs")
public class OperationLog {
    @TableId(type = IdType.INPUT) 
    public Long id;
    
    @TableField("user_id") 
    public Long userId;
    
    public String username;
    
    public String ip;
    
    public String method;
    
    public String path;
    
    public String params;
    
    public String result;
    
    @TableField("start_time") 
    public LocalDateTime startTime;
    
    @TableField("end_time") 
    public LocalDateTime endTime;
    
    @TableField("duration_ms") 
    public Long durationMs;
}
