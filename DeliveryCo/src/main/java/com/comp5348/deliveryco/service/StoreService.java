package com.comp5348.deliveryco.service;
import com.comp5348.Common.dto.DeliveryResponseDTO;
import com.comp5348.Common.model.DeliveryStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreService {
    private final RabbitTemplate rabbitTemplate;
    private final static String Response_QUEUE = "delivery.response.queue";

    @Autowired
    public StoreService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendResponseToStore(Long orderId, DeliveryStatus status) throws JsonProcessingException {
        DeliveryResponseDTO deliveryResponseDTO = new DeliveryResponseDTO();
        deliveryResponseDTO.setOrderId(orderId);
        deliveryResponseDTO.setDeliveryStatus(status);

        ObjectMapper mapper = new ObjectMapper();

        // 序列化为 JSON 字符串
        String jsonMessage = mapper.writeValueAsString(deliveryResponseDTO);

        // 发送 JSON 到 RabbitMQ 队列
        rabbitTemplate.convertAndSend(Response_QUEUE, jsonMessage);



    }

}
