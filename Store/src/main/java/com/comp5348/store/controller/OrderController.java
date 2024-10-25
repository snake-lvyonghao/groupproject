package com.comp5348.store.controller;

import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    // 创建一个日志对象


    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        // 输出日志信息，描述接收到的请求
        log.info("Received request to create order for goodsId: {}, customerId: {}, quantity: {}",
                request.goodsId, request.customerId, request.quantity);
        OrderDTO orderDTO = orderService.createOrder(request.goodsId, request.customerId, request.quantity);
        return ResponseEntity.ok(orderDTO);
    }

    public static class CreateOrderRequest {
        public Long goodsId;
        public Long customerId;
        public int quantity;
    }
}