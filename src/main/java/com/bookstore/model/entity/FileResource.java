package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

@TableName("file_resources")
public class FileResource {
    @TableId(type = IdType.INPUT) public Long id;
    @TableField("business_type") public String businessType;
    @TableField("original_name") public String originalName;
    @TableField("stored_name") public String storedName;
    @TableField("storage_path") public String storagePath;
    @TableField("access_url") public String accessUrl;
    @TableField("content_type") public String contentType;
    @TableField("size_bytes") public Long sizeBytes;
    @TableField("uploader_id") public Long uploaderId;
    @TableField("created_time") public LocalDateTime createdTime;
}
