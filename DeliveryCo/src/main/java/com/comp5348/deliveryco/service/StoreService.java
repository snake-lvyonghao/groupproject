package com.comp5348.deliveryco.service;
import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.comp5348.Common.dto.DeliveryResponseDTO;
import com.comp5348.Common.model.DeliveryStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class StoreService {
//    private final RabbitTemplate rabbitTemplate;
//    private final static String Response_QUEUE = "delivery.response.queue";
//
//    @Autowired
//    public StoreService(RabbitTemplate rabbitTemplate) {
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
//
//    public void sendResponseToStore(Long orderId,DeliveryStatus status) throws JsonProcessingException {
//        DeliveryResponseDTO deliveryResponseDTO = new DeliveryResponseDTO();
//        deliveryResponseDTO.setOrderId(orderId);
//        deliveryResponseDTO.setDeliveryStatus(status);
//
//        // 序列化为 JSON 字符串
//        String jsonMessage = mapper.writeValueAsString(deliveryResponseDTO);
//
//        // 发送 JSON 到 RabbitMQ 队列
//        rabbitTemplate.convertAndSend(Response_QUEUE, jsonMessage);
//
//
//
//    }
//
//    @Async
//    public void requestProcesser(DeliveryRequestDTO deliveryRequestDTO) throws JsonProcessingException, InterruptedException {
//        DeliveryStatus status;
//
//        Random random = new Random();
//        //用四次loop判断快递的状态，每次延迟3秒。
//        for(int i=0;i<4;i++){
//            if (i==0){status=DeliveryStatus.REQUEST_RECEIVED;}
//            else if (i==1) {status=DeliveryStatus.PREPARING;}
//            else if (i==2) {status=DeliveryStatus.SHIPPED;}
//            else {status=DeliveryStatus.DELIVERED;}
//
//            //每次循环5%的概率丢件，4次总共20%
//            int randomInt = random.nextInt(100);
//            if(randomInt<5){ status=DeliveryStatus.LOST; }
//
//
//            //每次状态变更延迟三秒钟.
//            Thread.sleep(3000);
//
//            //调用sendResponseToStore向Store发送message.
//            sendResponseToStore(deliveryRequestDTO.getOrderId(),status);
//
//
//        }
//    }
//
//    @RabbitListener(queues = "delivery.request.queue")
//    public void receiveDeliveryRequest(String message) {
//        try {
//            // 反序列化 JSON 为 DeliveryRequestDTO
//            DeliveryRequestDTO requestDTO = mapper.readValue(message, DeliveryRequestDTO.class);
//            System.out.println("Received delivery request: " + requestDTO);
//
//            // 这里进行相应的业务处理逻辑，例如更新状态、通知仓库等
//            requestProcesser(requestDTO);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//

}
