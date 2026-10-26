package com.nex.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_items")
public class OrderItem {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private String productCode;

    private BigDecimal productPrice;

    private Integer productQuantity;

    private BigDecimal itemTotalAmount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
