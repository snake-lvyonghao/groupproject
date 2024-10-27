package com.comp5348.store.service;

import com.comp5348.Common.config.MessagingConfig;
import com.comp5348.Common.dto.EmailRequestDTO;
import com.comp5348.Common.model.DeliveryStatus;
import com.comp5348.store.dto.OrderDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j(topic = "com.comp5348.store")
@Service
public class EmailService {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public EmailService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Async
    public void sendEmailRequest(OrderDTO orderDTO, DeliveryStatus status) throws JsonProcessingException {
        // 构建 DeliveryRequestDTO
        EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
        emailRequestDTO.setCustomerEmail(orderDTO.getCustomer().getEmail());
        emailRequestDTO.setCustomerName(orderDTO.getCustomer().getName());
        emailRequestDTO.setStatus(status);

        ObjectMapper mapper = new ObjectMapper();

        // Send customer name, Email address and delivery status to email
        // Serialize to JSON string
        String jsonMessage = mapper.writeValueAsString(emailRequestDTO);

        rabbitTemplate.convertAndSend(MessagingConfig.EMAIL_QUEUE, jsonMessage);
    }
}
