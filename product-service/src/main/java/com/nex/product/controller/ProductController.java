package com.nex.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nex.common.ApiResponse;
import com.nex.product.dto.ProductCreateDto;
import com.nex.product.dto.ProductUpdateDto;
import com.nex.product.entity.Product;
import com.nex.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    public ApiResponse<Long> create(@Valid @RequestBody ProductCreateDto request) {
        return productService.create(request);
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> getById(@PathVariable("id") Long id) {
        return ApiResponse.success(productService.getById(id));
    }

    @GetMapping("/listPage")
    public ApiResponse<IPage<Product>> page(@RequestParam(name = "current", defaultValue = "1") long current,
                                            @RequestParam(name = "size", defaultValue = "10") long size,
                                            @RequestParam(name = "keyword", required = false) String keyword,
                                            @RequestParam(name = "status", required = false) Integer status) {

        return productService.page(current, size, keyword, status);
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable("id") Long id, @Valid @RequestBody ProductUpdateDto request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<String> changeStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        return productService.changeStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable("id") Long id) {
        return productService.delete(id);
    }
}
