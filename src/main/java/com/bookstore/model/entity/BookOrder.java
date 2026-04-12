package com.bookstore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
public class BookOrder {
    @TableId(type = IdType.INPUT) public Long id;
    @TableField("order_no") public String orderNo;
    @TableField("user_id") public Long userId;
    public String status;
    @TableField("total_amount") public BigDecimal totalAmount;
    @TableField("receiver_name") public String receiverName;
    @TableField("receiver_phone") public String receiverPhone;
    @TableField("receiver_address") public String receiverAddress;
    public String remark;
    @TableField("created_time") public LocalDateTime createdTime;
    @TableField("updated_time") public LocalDateTime updatedTime;
}
