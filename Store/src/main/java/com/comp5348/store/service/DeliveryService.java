package com.comp5348.store.service;

import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.comp5348.Common.dto.DeliveryResponseDTO;
import com.comp5348.Common.model.DeliveryStatus;
import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.model.Order;
import com.comp5348.store.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.comp5348.Common.config.MessagingConfig.*;
import static com.comp5348.store.model.Order.OrderStatus.NON_REFUNDABLE;

@Slf4j(topic = "com.comp5348.store")
@Service
public class DeliveryService {
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;
    private final OrderRepository orderRepository;

    @Autowired
    public DeliveryService(RabbitTemplate rabbitTemplate,
                           EmailService emailService,
                           OrderRepository orderRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailService = emailService;
        this.orderRepository = orderRepository;
    }

    //Listen for the queue after 10 seconds
    @RabbitListener(queues = PACK_QUEUE)
    public void processDeliveryQueue(String message) {
        try {
            log.info("PACK_QUEUE");
            DeliveryRequestDTO requestDTO = mapper.readValue(message, DeliveryRequestDTO.class);
            Optional<Order> orderOptional = orderRepository.findById(requestDTO.getOrderId());

            if (orderOptional.isEmpty()) {
                log.info("Order {} not found, skip delivery request.", requestDTO.getOrderId());
                return;
            }

            Order order = orderOptional.get();

            // Perform a final status check before continuing
            switch (order.getStatus()) {
                case CANCELED:
                case NON_REFUNDABLE:
                    log.info("Order {} has been cancelled or is non-refundable, skip delivery request.", order.getId());
                    return;
                default:
                    break;
            }


            ObjectMapper mapper = new ObjectMapper();
            // Forward to the delivery service as the order has not been cancelled or is non-refundable
            String jsonMessage = mapper.writeValueAsString(requestDTO);

            // 转发给交付服务，因为订单未被取消或不可退款
            order.setStatus(NON_REFUNDABLE);
            orderRepository.save(order);
            log.info("订单已发送不可撤销 {}", requestDTO.getOrderId());
            rabbitTemplate.convertAndSend(DELIVERY_QUEUE, jsonMessage);
        } catch (Exception e) {
            log.error("An error occurred processing the delivery queue：", e);
        }
    }
    @Async
    public void sendDeliveryRequest(OrderDTO orderDTO) throws JsonProcessingException {
        // Build the DeliveryRequestDTO
        DeliveryRequestDTO deliveryRequestDTO = new DeliveryRequestDTO();
        deliveryRequestDTO.setOrderId(orderDTO.getId());

        ObjectMapper mapper = new ObjectMapper();

        // Send order information to DeliveryCo
        // Serialize to JSON string
        String jsonMessage = mapper.writeValueAsString(deliveryRequestDTO);

        rabbitTemplate.convertAndSend(DELAY_QUEUE, jsonMessage);
    }

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = DELIVERR_RESPONSE_QUEUE)
    public void receiveDeliveryResponse(String message) {
        try {
            log.info("DELIVERR_RESPONSE_QUEUE");
            // Deserialize JSON to DeliveryResponseDTO
            DeliveryResponseDTO responseDTO = mapper.readValue(message, DeliveryResponseDTO.class);
            Long orderId = responseDTO.getOrderId();
            DeliveryStatus deliveryStatus = responseDTO.getDeliveryStatus();

            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isEmpty()) {
                log.info("Order {} not found, skip processing.", orderId);
                return;
            }

            Order order = orderOptional.get();

            // Get order information and send email notifications
            OrderDTO orderDTO = new OrderDTO(order, true);
            emailService.sendEmailRequest(orderDTO, deliveryStatus);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse delivery response message", e);
        } catch (Exception e) {
            log.error("Unexpected error while processing delivery response", e);
        }
    }
}
