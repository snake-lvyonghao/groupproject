package com.comp5348.store.service;

import com.comp5348.Common.dto.EmailRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Email;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final RabbitTemplate rabbitTemplate;
    private final static String STORE_TO_EMAIL_QUEUE = "store.to.email.queue";

    @Autowired
    public EmailService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEmail(Long customerId, String content) throws JsonProcessingException {

        EmailRequestDTO emailRequestDTO = new EmailRequestDTO(customerId, content);

        ObjectMapper mapper = new ObjectMapper();

        // 序列化为 JSON 字符串
        String jsonMessage = mapper.writeValueAsString(emailRequestDTO);

        // 发送 JSON 到 RabbitMQ 队列
        rabbitTemplate.convertAndSend(STORE_TO_EMAIL_QUEUE, jsonMessage);
    }
}
