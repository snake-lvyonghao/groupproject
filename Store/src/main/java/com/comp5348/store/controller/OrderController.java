package com.comp5348.store.controller;

import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

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
    @PostMapping("/refund")
    public ResponseEntity<String> refundOrder(@RequestBody RefundOrderRequest request) {
        Long orderId = request.orderId;
        log.info("Received request to refund order for orderId: {}", request.orderId);

        if (orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order ID cannot be null.");
        }

        boolean refundSuccessful = orderService.cancelOrder(orderId);

        if (refundSuccessful) {
            return ResponseEntity.ok("Refund successful for order ID: " + orderId);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refund failed for order ID: " + orderId);
        }
    }

    public static class CreateOrderRequest {
        public Long goodsId;
        public Long customerId;
        public int quantity;
    }

    public static class RefundOrderRequest {
        public Long orderId;
    }

}