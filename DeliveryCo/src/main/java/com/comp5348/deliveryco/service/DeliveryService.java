package com.comp5348.deliveryco.service;

import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = "delivery.request.queue")
    public void receiveDeliveryRequest(String message) {
        try {
            // 反序列化 JSON 为 DeliveryRequestDTO
            DeliveryRequestDTO requestDTO = mapper.readValue(message, DeliveryRequestDTO.class);
            System.out.println("Received delivery request: " + requestDTO);

            // 这里进行相应的业务处理逻辑，例如更新状态、通知仓库等
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
