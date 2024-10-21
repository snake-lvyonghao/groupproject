package com.comp5348.store.service;

import com.comp5348.store.dto.OrderDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {
    private final RabbitTemplate rabbitTemplate;
    private final static String DELIVERY_QUEUE = "delivery.request";

    @Autowired
    public DeliveryService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDeliveryRequest(OrderDTO orderDTO) {
        // 发送订单信息给DeliveryCo
        rabbitTemplate.convertAndSend(DELIVERY_QUEUE, orderDTO);
        System.out.println("Delivery request sent for order ID: " + orderDTO.getId());
    }
}
