package com.nex.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal orderTotalAmount;

    private Integer orderStatus;

    private String orderRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
