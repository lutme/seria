package com.nex.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStockAdjustDto {

    /**
     * 正数增加库存, 负数减少库存
     * 例如: 100 表示增加 100, -5 表示减少 5
     */
    @NotNull(message = "库存调整数量不能为空")
    private Integer delta;

}
