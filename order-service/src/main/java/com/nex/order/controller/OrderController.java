package com.nex.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nex.common.ApiResponse;
import com.nex.order.dto.OrderCreateDto;
import com.nex.order.entity.Order;
import com.nex.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public ApiResponse<Long> create(@Valid @RequestBody OrderCreateDto dto) {

        Long orderId = orderService.create(dto);

        return ApiResponse.success(orderId, "订单创建成功");
    }

    /**
     * 根据订单ID查询订单
     */
    @GetMapping("/{id}")
    public ApiResponse<Order> getById(@PathVariable("id") Long id) {

        return ApiResponse.success(orderService.getById(id));
    }

    /**
     * 分页查询订单
     */
    @GetMapping
    public ApiResponse<IPage<Order>> page(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) Integer status) {

        return ApiResponse.success(orderService.page(current, size, userId, status));
    }
}
