package com.comp5348.emailService;

import com.comp5348.Common.config.DeliveryStatus;
import com.comp5348.Common.config.MessagingConfig;
import com.comp5348.Common.dto.EmailRequestDTO;
import com.comp5348.emailService.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EmailRequestTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private EmailService emailService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testReceiveDeliveryRequest() throws JsonProcessingException {
        String address = "123456@123.123";
        String name = "456";
        int status = DeliveryStatus.FETCHED.getCode();
        EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
        emailRequestDTO.setCustomerEmail(address);
        emailRequestDTO.setCustomerName(name);
        emailRequestDTO.setDeliveryStatus(status);
        // 调用被测试方法

        String jsonMessage = objectMapper.writeValueAsString(emailRequestDTO);

        // 发送 JSON 到 RabbitMQ 队列
        rabbitTemplate.convertAndSend(MessagingConfig.EMAIL_QUEUE, jsonMessage);
    }
}
