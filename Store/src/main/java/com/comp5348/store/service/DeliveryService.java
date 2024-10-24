package com.comp5348.store.service;

import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.comp5348.store.dto.OrderDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {
    private final RabbitTemplate rabbitTemplate;
    private final static String DELIVERY_QUEUE = "delivery.request.queue";

    @Autowired
    public DeliveryService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDeliveryRequest(OrderDTO orderDTO) throws JsonProcessingException {
        // 构建 DeliveryRequestDTO
        DeliveryRequestDTO deliveryRequestDTO = new DeliveryRequestDTO();
        deliveryRequestDTO.setOrderId(orderDTO.getId());

        ObjectMapper mapper = new ObjectMapper();
        // JavaTimeModule is registered to handle the serialization of LocalDateTime.

        // 将 OrderDTO 的信息转换为 WarehouseInfo 列表
//        List<DeliveryRequestDTO.WarehouseInfo> warehouseInfos = orderDTO.getOrderWarehouses().stream()
//                .map(orderWarehouseDTO -> new DeliveryRequestDTO.WarehouseInfo(
//                        orderWarehouseDTO.getWarehouseGoodsDTO().getWarehouse().getName(),
//                        orderWarehouseDTO.getWarehouseGoodsDTO().getWarehouse().getLocation(),
//                        orderWarehouseDTO.getWarehouseGoodsDTO().getGoods().getName(),
//                        orderWarehouseDTO.getQuantity()
//                ))
//                .collect(Collectors.toList());
//        deliveryRequestDTO.setWarehouseInfos(warehouseInfos);

        // 发送订单信息给DeliveryCo
        // 序列化为 JSON 字符串
        String jsonMessage = mapper.writeValueAsString(deliveryRequestDTO);

        // 发送 JSON 到 RabbitMQ 队列
        rabbitTemplate.convertAndSend("delivery.request.queue", jsonMessage);
    }
}
