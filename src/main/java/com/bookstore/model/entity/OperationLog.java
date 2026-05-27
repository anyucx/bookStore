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
@TableName("operation_logs")
public class OperationLog {
    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("user_id")
    private Long userId;
    private String username;
    private String ip;
    private String method;
    private String path;
    private String params;
    private String result;
    @TableField("start_time")
    private LocalDateTime startTime;
    @TableField("end_time")
    private LocalDateTime endTime;
    @TableField("duration_ms")
    private Long durationMs;
}
