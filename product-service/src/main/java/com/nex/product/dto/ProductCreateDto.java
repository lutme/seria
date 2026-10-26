package com.nex.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateDto {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String productName;

    @NotBlank(message = "商品编码不能为空")
    @Size(max = 64, message = "商品编码不能超过64个字符")
    private String productCode;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal productPrice;

    @NotNull(message = "初始库存不能为空")
    @PositiveOrZero(message = "初始库存不能小于0")
    private Integer initialStock;
}
