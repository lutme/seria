package com.nex.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("products")
@Data
public class Product {

    /**
     * 雪花算法生成的用户ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
}
