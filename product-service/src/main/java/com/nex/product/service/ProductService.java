package com.nex.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nex.common.ApiResponse;
import com.nex.common.snowflake.SnowflakeIdGenerator;
import com.nex.product.dto.ProductCreateDto;
import com.nex.product.dto.ProductUpdateDto;
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
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductStockMapper productStockMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 创建商品, 并初始化库存记录
     * 两张表必须在同一数据库事务中提交
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Long> create(ProductCreateDto dto) {

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductCode, dto.getProductCode());

        if (productMapper.selectCount(queryWrapper) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "商品编码已存在");
        }


        Product product = new Product();
        product.setId(snowflakeIdGenerator.nextId());
        product.setProductName(dto.getProductName());
        product.setProductCode(dto.getProductCode());
        product.setProductPrice(dto.getProductPrice());
        product.setProductStatus(1);

        productMapper.insert(product);

        ProductStock stock = new ProductStock();
        stock.setId(snowflakeIdGenerator.nextId());
        stock.setProductId(product.getId());
        stock.setProductStockQuantity(dto.getInitialStock());
        stock.setProductStockLocked(0);
        stock.setProductStockVersion(0);

        productStockMapper.insert(stock);

        return ApiResponse.success(product.getId());
    }

    /**
     * 查询商品详情
     */
    public Product getById(Long id) {

        Product product = productMapper.selectById(id);

        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }

        return product;
    }

    /**
     * 分页查询商品
     */
    public ApiResponse<IPage<Product>> page(long current, long size, String keyword, Integer status) {

        if (current < 1 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "页码必须大于 0, 分页大小必须在 1 到 100 之间");
        }

        if (status != null && status != 0 && status != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "商品状态只能为 0 或 1"
            );
        }

        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();

        query.like(keyword != null && !keyword.isBlank(), Product::getProductName, keyword);
        query.eq(status != null, Product::getProductStatus, status);
        query.orderByDesc(Product::getCreateTime);

        return ApiResponse.success(productMapper.selectPage(new Page<>(current, size), query));
    }

    /**
     * 修改商品名称和价格
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductUpdateDto dto) {
        Product product = getById(id);

        product.setProductName(dto.getProductName());
        product.setProductPrice(dto.getProductPrice());

        productMapper.updateById(product);
    }

    /**
     * 上架或下架
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "商品状态只能为 0 或 1"
            );
        }

        Product product = getById(id);
        product.setProductStatus(status);
        productMapper.updateById(product);
    }

    /**
     * 删除商品, 同时删除库存记录
     * 将来有订单引用商品时, 需要重新评估删除规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);

        LambdaQueryWrapper<ProductStock> stockQuery =
                new LambdaQueryWrapper<>();
        stockQuery.eq(ProductStock::getProductId, id);

        productStockMapper.delete(stockQuery);
        productMapper.deleteById(id);
    }
}
