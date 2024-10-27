package com.comp5348.deliveryco.service;
import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.comp5348.Common.dto.DeliveryResponseDTO;
import com.comp5348.Common.model.DeliveryStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.comp5348.Common.config.MessagingConfig.DELIVERR_RESPONSE_QUEUE;
import static com.comp5348.Common.config.MessagingConfig.DELIVERY_QUEUE;

@Service
@Slf4j(topic = "com.comp5348.deliveryco")
public class StoreService {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public StoreService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void sendResponseToStore(Long orderId,DeliveryStatus status) throws JsonProcessingException {
        DeliveryResponseDTO deliveryResponseDTO = new DeliveryResponseDTO();
        deliveryResponseDTO.setOrderId(orderId);
        deliveryResponseDTO.setDeliveryStatus(status);

        // Serialize to JSON string
        String jsonMessage = mapper.writeValueAsString(deliveryResponseDTO);

        // Send JSON to the RabbitMQ queue
        rabbitTemplate.convertAndSend(DELIVERR_RESPONSE_QUEUE, jsonMessage);
    }

    @Async
    public void requestProcesser(DeliveryRequestDTO deliveryRequestDTO) throws JsonProcessingException, InterruptedException {
        Random random = new Random();
        Map<Integer, DeliveryStatus> statusMap = new HashMap<>();
        statusMap.put(0, DeliveryStatus.REQUEST_RECEIVED);
        statusMap.put(1, DeliveryStatus.PREPARED);
        statusMap.put(2, DeliveryStatus.SHIPPED);
        statusMap.put(3, DeliveryStatus.DELIVERED);

        // set delay
        for (int i = 0; i < 4; i++) {
            // There is a 5% chance of losing the item per cycle, and a total of 20% for 4 cycles
            if (random.nextInt(100) < 5) {
                DeliveryStatus status = DeliveryStatus.LOST;
                log.info("Order {} has been lost, stop further processing.", deliveryRequestDTO.getOrderId());
                sendResponseToStore(deliveryRequestDTO.getOrderId(), status);
                return;
            }

            DeliveryStatus status = statusMap.getOrDefault(i, DeliveryStatus.DELIVERED);

            // Log status updates
            log.info("The current status of order {} is: {}", deliveryRequestDTO.getOrderId(), status);

// Each status change is delayed for three seconds.
            Thread.sleep(3000);

            // 调用 sendResponseToStore 向 Store 发送 message。
            sendResponseToStore(deliveryRequestDTO.getOrderId(), status);
        }
    }

    @RabbitListener(queues = DELIVERY_QUEUE)
    public void receiveDeliveryRequest(String message) {
        try {
            // Deserialize the JSON to DeliveryRequestDTO
            DeliveryRequestDTO requestDTO = mapper.readValue(message, DeliveryRequestDTO.class);
            System.out.println("Received delivery request: " + requestDTO.getOrderId());
            requestProcesser(requestDTO);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
