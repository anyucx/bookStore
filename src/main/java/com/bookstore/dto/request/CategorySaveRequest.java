package com.bookstore.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CategorySaveRequest {
    private Long id;
    private Long parentId;
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private Integer sort;
    private Integer status;
}
