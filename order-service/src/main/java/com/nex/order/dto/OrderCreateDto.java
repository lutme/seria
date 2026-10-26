package com.nex.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDto {

    @NotNull(message = "用户 ID 不能为空")
    @Positive(message = "用户 ID 必须大于 0")
    private Long userId;

    @Size(max = 500, message = "订单备注不能超过 500 个字符")
    private String orderRemark;

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {

        @NotNull(message = "商品 ID 不能为空")
        @Positive(message = "商品 ID 必须大于 0")
        private Long productId;

        @NotBlank(message = "商品名称不能为空")
        @Size(max = 100, message = "商品名称不能超过 100 个字符")
        private String productName;

        @NotBlank(message = "商品编码不能为空")
        @Size(max = 64, message = "商品编码不能超过 64 个字符")
        private String productCode;

        @NotNull(message = "商品单价不能为空")
        @DecimalMin(value = "0.01", message = "商品单价必须大于 0")
        private BigDecimal productPrice;

        @NotNull(message = "购买数量不能为空")
        @Positive(message = "购买数量必须大于 0")
        private Integer productQuantity;
    }
}
