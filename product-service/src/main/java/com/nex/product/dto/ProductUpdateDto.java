package com.nex.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateDto {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String productName;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal productPrice;
}
