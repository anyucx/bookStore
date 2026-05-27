package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("orders")
public class BookOrder {
    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("order_no")
    private String orderNo;
    @TableField("user_id")
    private Long userId;
    private String status;
    @TableField("total_amount")
    private BigDecimal totalAmount;
    @TableField("receiver_name")
    private String receiverName;
    @TableField("receiver_phone")
    private String receiverPhone;
    @TableField("receiver_address")
    private String receiverAddress;
    private String remark;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
