package com.comp5348.emailService.service;

import com.comp5348.Common.config.DeliveryStatus;
import com.comp5348.Common.config.MessagingConfig;
import com.comp5348.Common.dto.EmailRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.rpc.context.AttributeContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EmailService
{
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger logger = LogManager.getLogger(EmailService.class);
    @RabbitListener(queues = MessagingConfig.EMAIL_QUEUE)
    public EmailRequestDTO receiveEmailRequest(String message) {
        try {
            // 反序列化 JSON 为 DeliveryRequestDTO
            EmailRequestDTO requestDTO = mapper.readValue(message, EmailRequestDTO.class);
            logger.info("Received email request: " + requestDTO);
            sendEmail(requestDTO);
            return requestDTO;
            // 这里进行相应的业务处理逻辑，例如更新状态、通知仓库等
        } catch (Exception e) {
            logger.error("receive email error:" + e);
            return null;
        }
    }

    public void sendEmail(EmailRequestDTO requestDTO)
    {
        logger.info("Send email to address: " + requestDTO.getCustomerEmail());
        String context = "Email: " + requestDTO.getCustomerName() + ", " +
                DeliveryStatus.getMsgFromCode(requestDTO.getDeliveryStatus());
        logger.info("Email context: " + context);

    }



}
