package com.nex.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("product_stocks")
@Data
public class ProductStock {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private Long productId;

    /**
     * 当前可用库存
     */
    private Integer productStockQuantity;

    /**
     * 已锁定库存
     */
    private Integer productStockLocked;

    /**
     * 乐观锁版本号
     */
    private Integer productStockVersion;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
