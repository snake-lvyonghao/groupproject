package com.comp5348.emailService.service;

import com.comp5348.Common.config.MessagingConfig;
import com.comp5348.Common.dto.EmailRequestDTO;
import com.comp5348.Common.model.DeliveryStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j(topic = "com.comp5348.emailService")
public class EmailService
{
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = MessagingConfig.EMAIL_QUEUE)
    public void receiveEmailRequest(String message) {
        try {
            // Deserialize the JSON to DeliveryRequestDTO
            EmailRequestDTO requestDTO = mapper.readValue(message, EmailRequestDTO.class);
            log.info("Received email request: " + requestDTO.getCustomerEmail());
            sendEmail(requestDTO);
        } catch (Exception e) {
            log.error("receive email error:" + e);
        }
    }

    public void sendEmail(EmailRequestDTO requestDTO) {
        log.info("Send email to address: " + requestDTO.getCustomerEmail());

        Map<DeliveryStatus, String> statusMessages = Map.of(
                DeliveryStatus.DELIVERED, "delivered",
                DeliveryStatus.PREPARED, "prepared",
                DeliveryStatus.REQUEST_RECEIVED, "received",
                DeliveryStatus.LOST, "lost",
                DeliveryStatus.SHIPPED, "shipped",
                DeliveryStatus.CANCELLED, "cancelled"
        );

        String statusMessage = statusMessages.getOrDefault(requestDTO.getStatus(), "unknown status");
        String context = "Email: " + requestDTO.getCustomerEmail() + ", CurrentOrder Status is" + " "+ statusMessage;

        log.info("Email context: " + context);
    }

}
