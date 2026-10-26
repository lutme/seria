package com.nex.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nex.product.entity.ProductStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductStockMapper extends BaseMapper<ProductStock> {

    @Update("""
        update product_stocks
            set product_stock_quantity = product_stock_quantity + #{delta},
                product_stock_version = product_stock_version + 1
        where product_id = #{productId}
            and product_stock_quantity + #{delta} >= 0
    """)
    int adjustAvailableStock(@Param("productId") Long productId, @Param("delta") int delta);

}
