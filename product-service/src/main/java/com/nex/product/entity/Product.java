package com.nex.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("products")
@Data
public class Product {

    /**
     * 雪花算法生成的用户ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private String productName;

    private String productCode;

    private BigDecimal productPrice;

    /**
     * 1: 上架; 0: 下架
     */
    private Integer productStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
