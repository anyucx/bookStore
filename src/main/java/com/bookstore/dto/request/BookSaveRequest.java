package com.bookstore.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BookSaveRequest {
    private Long id;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    @NotBlank(message = "图书名称不能为空")
    private String name;
    private String author;
    private String isbn;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    @NotNull(message = "库存不能为空")
    private Integer stock;
    private String coverUrl;
    private String description;
    private Integer status;
}
