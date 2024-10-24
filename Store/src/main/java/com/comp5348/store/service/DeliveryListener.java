package com.comp5348.store.service;

import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.comp5348.Common.dto.DeliveryResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DeliveryListener {


    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = "delivery.response.queue")
    public void receiveDeliveryResponse(String message) {
        try {
            // 反序列化 JSON 为 DeliveryResponseDTO
            DeliveryResponseDTO requestDTO = mapper.readValue(message, DeliveryResponseDTO.class);
            System.out.println("Received delivery response: " + requestDTO);

            // 这里写的是在Store收到DeliveryCo的消息之后的逻辑。
            //调用emailService给用户发现消息。

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
