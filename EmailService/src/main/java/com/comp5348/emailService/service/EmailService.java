package com.comp5348.emailService.service;

import com.comp5348.Common.config.MessagingConfig;
import com.comp5348.Common.dto.EmailRequestDTO;
import com.comp5348.Common.model.DeliveryStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailService
{
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @RabbitListener(queues = MessagingConfig.EMAIL_QUEUE)
    public void receiveEmailRequest(String message) {
        try {
            // 反序列化 JSON 为 DeliveryRequestDTO
            EmailRequestDTO requestDTO = mapper.readValue(message, EmailRequestDTO.class);
            logger.info("Received email request: " + requestDTO.getCustomerEmail());
            sendEmail(requestDTO);
            // 这里进行相应的业务处理逻辑，例如更新状态、通知仓库等
        } catch (Exception e) {
            logger.error("receive email error:" + e);
        }
    }

    public void sendEmail(EmailRequestDTO requestDTO) {
        logger.info("Send email to address: " + requestDTO.getCustomerEmail());

        Map<DeliveryStatus, String> statusMessages = Map.of(
                DeliveryStatus.DELIVERED, "delivered",
                DeliveryStatus.PREPARED, "prepared",
                DeliveryStatus.REQUEST_RECEIVED, "received",
                DeliveryStatus.LOST, "lost",
                DeliveryStatus.SHIPPED, "shipped",
                DeliveryStatus.CANCELLED, "cancelled"
        );

        String statusMessage = statusMessages.getOrDefault(requestDTO.getStatus(), "unknown status");
        String context = "Email: " + requestDTO.getCustomerName() + ", " + statusMessage;

        logger.info("Email context: " + context);
    }

}
