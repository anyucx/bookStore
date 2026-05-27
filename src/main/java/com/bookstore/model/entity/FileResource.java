package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("file_resources")
public class FileResource {
    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("business_type")
    private String businessType;
    @TableField("original_name")
    private String originalName;
    @TableField("stored_name")
    private String storedName;
    @TableField("storage_path")
    private String storagePath;
    @TableField("access_url")
    private String accessUrl;
    @TableField("content_type")
    private String contentType;
    @TableField("size_bytes")
    private Long sizeBytes;
    @TableField("uploader_id")
    private Long uploaderId;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
