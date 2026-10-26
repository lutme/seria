package com.nex.product.controller;

import com.nex.common.ApiResponse;
import com.nex.product.dto.ProductStockAdjustDto;
import com.nex.product.entity.ProductStock;
import com.nex.product.service.ProductStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/{productId}/stock")
@RequiredArgsConstructor
public class ProductStockController {

    private final ProductStockService productStockService;

    @GetMapping
    public ApiResponse<ProductStock> getStock(@PathVariable("productId") Long productId) {

        return ApiResponse.success(productStockService.getByProductId(productId));
    }

    @PostMapping
    public ApiResponse<String> adjustStock(@PathVariable("productId") Long productId,
                                           @Valid @RequestBody ProductStockAdjustDto dto) {

        productStockService.adjust(productId, dto);

        return ApiResponse.success("库存调整成功");
    }
}
