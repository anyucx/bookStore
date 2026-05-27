package com.bookstore.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AdminOrderUpdateRequest {
    @NotNull(message = "订单ID不能为空")
    private Long id;
    @NotBlank(message = "状态不能为空")
    private String status;
}
