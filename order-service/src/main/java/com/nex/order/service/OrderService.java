package com.nex.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nex.common.snowflake.SnowflakeIdGenerator;
import com.nex.order.dto.OrderCreateDto;
import com.nex.order.entity.Order;
import com.nex.order.entity.OrderItem;
import com.nex.order.mapper.OrderItemMapper;
import com.nex.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderCreateDto request) {
        LocalDateTime now = LocalDateTime.now();

        Order order = new Order();
        order.setId(snowflakeIdGenerator.nextId());
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setOrderRemark(request.getOrderRemark());
        order.setOrderStatus(0);
        order.setCreateTime(now);
        order.setUpdateTime(now);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderCreateDto.OrderItemDto itemRequest
                : request.getItems()) {

            BigDecimal itemAmount = itemRequest.getProductPrice()
                    .multiply(BigDecimal.valueOf(
                            itemRequest.getProductQuantity()));

            totalAmount = totalAmount.add(itemAmount);
        }

        order.setOrderTotalAmount(totalAmount);
        orderMapper.insert(order);

        for (OrderCreateDto.OrderItemDto itemRequest
                : request.getItems()) {

            OrderItem item = new OrderItem();
            item.setId(snowflakeIdGenerator.nextId());
            item.setOrderId(order.getId());
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setProductCode(itemRequest.getProductCode());
            item.setProductPrice(itemRequest.getProductPrice());
            item.setProductQuantity(itemRequest.getProductQuantity());

            item.setItemTotalAmount(
                    itemRequest.getProductPrice()
                            .multiply(BigDecimal.valueOf(
                                    itemRequest.getProductQuantity())));

            item.setCreateTime(now);
            item.setUpdateTime(now);

            orderItemMapper.insert(item);
        }

        return order.getId();
    }

    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);

        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }

        return order;
    }

    public IPage<Order> page(long current, long size, Long userId, Integer status) {

        if (current < 1 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分页参数不合法");
        }

        if (status != null && status != 0 && status != 1 && status != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态不合法");
        }

        Page<Order> page = new Page<>(current, size);

        return orderMapper.selectPage(
                page,
                new com.baomidou.mybatisplus.core.conditions.query
                        .LambdaQueryWrapper<Order>()
                        .eq(userId != null, Order::getUserId, userId)
                        .eq(status != null, Order::getOrderStatus, status)
                        .orderByDesc(Order::getCreateTime));
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return time + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();
    }
}
