package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("payment_records")
public class PaymentRecord {
    @TableId(type = IdType.INPUT) public Long id;
    @TableField("order_id") public Long orderId;
    @TableField("pay_channel") public String payChannel;
    @TableField("pay_status") public String payStatus;
    @TableField("transaction_no") public String transactionNo;
    public BigDecimal amount;
    @TableField("callback_content") public String callbackContent;
    @TableField("paid_time") public LocalDateTime paidTime;
    @TableField("created_time") public LocalDateTime createdTime;
    @TableField("updated_time") public LocalDateTime updatedTime;
}
