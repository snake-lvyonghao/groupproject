package com.comp5348.emailService.service;

import com.comp5348.Common.config.MessagingConfig;
import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.comp5348.Common.dto.EmailRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class EmailService
{
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = MessagingConfig.EMAIL_QUEUE)
    public void receiveDeliveryRequest(String message) {
        try {
            // 反序列化 JSON 为 DeliveryRequestDTO
            EmailRequestDTO requestDTO = mapper.readValue(message, EmailRequestDTO.class);
            System.out.println("Received email request: " + requestDTO);

            // 这里进行相应的业务处理逻辑，例如更新状态、通知仓库等
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
