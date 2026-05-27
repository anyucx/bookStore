package com.bookstore.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserUpdateRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    private String displayName;
    private Integer status;
    private Long roleId;
}
