package com.bookstore.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "收货人不能为空")
    private String receiverName;
    @NotBlank(message = "手机号不能为空")
    private String receiverPhone;
    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;
    private String remark;
}
