package com.comp5348.store.service;

import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.comp5348.Common.dto.EmailRequestDTO;
import com.comp5348.store.dto.OrderDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class EmailService {
    private final RabbitTemplate rabbitTemplate;
    private final static String EMAIL_QUEUE = "email.request.queue";

    @Autowired
    public EmailService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEmailRequest(OrderDTO orderDTO) throws JsonProcessingException {
        // 构建 DeliveryRequestDTO
        EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
        emailRequestDTO.setCustomerEmail(orderDTO.getCustomer().getEmail());
        emailRequestDTO.setCustomerName(orderDTO.getCustomer().getName());


        ObjectMapper mapper = new ObjectMapper();

        // 发送顾客姓名，邮箱地址，给Email
        // 序列化为 JSON 字符串
        String jsonMessage = mapper.writeValueAsString(emailRequestDTO);

        // 发送 JSON 到 RabbitMQ 队列
        rabbitTemplate.convertAndSend("email.request.queue", jsonMessage);
    }
}
