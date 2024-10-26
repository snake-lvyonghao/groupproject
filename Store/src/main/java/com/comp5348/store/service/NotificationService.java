package com.comp5348.store.service;

import com.comp5348.Common.model.DeliveryStatus;
import com.comp5348.store.dto.OrderDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final DeliveryService deliveryService;
    private final EmailService emailService;

    @Autowired
    public NotificationService(DeliveryService deliveryService, EmailService emailService) {
        this.deliveryService = deliveryService;
        this.emailService = emailService;
    }

    public void sendDeliveryRequest(OrderDTO orderDTO) throws JsonProcessingException {
        deliveryService.sendDeliveryRequest(orderDTO);
    }

    public void sendEmail(OrderDTO orderDTO, DeliveryStatus deliveryStatus) throws JsonProcessingException {
        emailService.sendEmailRequest(orderDTO, deliveryStatus);
    }
}
