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

@Service
public class StoreService {
    private final RabbitTemplate rabbitTemplate;
    private final static String Response_QUEUE = "delivery.response.queue";

    @Autowired
    public StoreService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendResponseToStore(Long orderId,DeliveryStatus status) throws JsonProcessingException {
        DeliveryResponseDTO deliveryResponseDTO = new DeliveryResponseDTO();
        deliveryResponseDTO.setOrderId(orderId);
        deliveryResponseDTO.setDeliveryStatus(status);

        ObjectMapper mapper = new ObjectMapper();

        // 序列化为 JSON 字符串
        String jsonMessage = mapper.writeValueAsString(deliveryResponseDTO);

        // 发送 JSON 到 RabbitMQ 队列
        rabbitTemplate.convertAndSend(Response_QUEUE, jsonMessage);



    }

    @Async
    public void requestProcesser(DeliveryRequestDTO deliveryRequestDTO) throws JsonProcessingException, InterruptedException {
        DeliveryStatus status;

        //用四次loop判断快递的状态，每次延迟3秒。
        for(int i=0;i<4;i++){
            if (i==0){status=DeliveryStatus.REQUEST_RECEIVED;}
            else if (i==1) {status=DeliveryStatus.PREPARING;}
            else if (i==2) {status=DeliveryStatus.SHIPPED;}
            else {status=DeliveryStatus.DELIVERED;}

            //每次状态变更延迟三秒钟.
            Thread.sleep(3000);

            //调用sendResponseToStore向Store发送message.
            sendResponseToStore(deliveryRequestDTO.getOrderId(),status);


        }
    }

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = "delivery.request.queue")
    public void receiveDeliveryRequest(String message) {
        try {
            // 反序列化 JSON 为 DeliveryRequestDTO
            DeliveryRequestDTO requestDTO = mapper.readValue(message, DeliveryRequestDTO.class);
            System.out.println("Received delivery request: " + requestDTO);

            // 这里进行相应的业务处理逻辑，例如更新状态、通知仓库等

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(Long orderId,DeliveryStatus status) {

    }




}
