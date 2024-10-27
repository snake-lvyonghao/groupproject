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

        // 序列化为 JSON 字符串
        String jsonMessage = mapper.writeValueAsString(deliveryResponseDTO);

        // 发送 JSON 到 RabbitMQ 队列
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

        // 用四次 loop 判断快递的状态，每次延迟 3 秒。
        for (int i = 0; i < 4; i++) {
            // 每次循环 5% 的概率丢件，4 次总共 20%
            if (random.nextInt(100) < 5) {
                DeliveryStatus status = DeliveryStatus.LOST;
                log.info("订单 {} 已丢失，停止后续处理。", deliveryRequestDTO.getOrderId());
                sendResponseToStore(deliveryRequestDTO.getOrderId(), status);
                return;
            }

            DeliveryStatus status = statusMap.getOrDefault(i, DeliveryStatus.DELIVERED);

            // 日志记录状态更新
            log.info("订单 {} 的当前状态为：{}", deliveryRequestDTO.getOrderId(), status);

            // 每次状态变更延迟三秒钟。
            Thread.sleep(3000);

            // 调用 sendResponseToStore 向 Store 发送 message。
            sendResponseToStore(deliveryRequestDTO.getOrderId(), status);
        }
    }

    @RabbitListener(queues = DELIVERY_QUEUE)
    public void receiveDeliveryRequest(String message) {
        try {
            // 反序列化 JSON 为 DeliveryRequestDTO
            DeliveryRequestDTO requestDTO = mapper.readValue(message, DeliveryRequestDTO.class);
            System.out.println("Received delivery request: " + requestDTO.getOrderId());

            // 这里进行相应的业务处理逻辑，例如更新状态、通知仓库等
            requestProcesser(requestDTO);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
