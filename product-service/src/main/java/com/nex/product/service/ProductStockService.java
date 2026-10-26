package com.nex.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nex.product.dto.ProductStockAdjustDto;
import com.nex.product.entity.Product;
import com.nex.product.entity.ProductStock;
import com.nex.product.mapper.ProductMapper;
import com.nex.product.mapper.ProductStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductStockService {

    private final ProductMapper productMapper;
    private final ProductStockMapper productStockMapper;

    public ProductStock getByProductId(Long productId) {

        Product product = productMapper.selectById(productId);

        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }

        LambdaQueryWrapper<ProductStock> query = new LambdaQueryWrapper<>();
        query.eq(ProductStock::getProductId, productId);

        ProductStock stock = productStockMapper.selectOne(query);

        if (stock == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品库存记录不存在");
        }

        return stock;
    }

    /**
     * 后台调整可用库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjust(Long productId, ProductStockAdjustDto request) {

        if (request.getDelta() == null || request.getDelta() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "库存调整数量不能为 0");
        }

        // 校验商品和库存记录是否存在
        getByProductId(productId);

        int affectedRows = productStockMapper.adjustAvailableStock(productId, request.getDelta());

        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "库存调整失败, 库存不足或库存记录已发生变化");
        }
    }
}
