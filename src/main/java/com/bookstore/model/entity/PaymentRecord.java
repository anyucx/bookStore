package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("payment_records")
public class PaymentRecord {
    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("pay_channel")
    private String payChannel;
    @TableField("pay_status")
    private String payStatus;
    @TableField("transaction_no")
    private String transactionNo;
    private BigDecimal amount;
    @TableField("callback_content")
    private String callbackContent;
    @TableField("paid_time")
    private LocalDateTime paidTime;
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
