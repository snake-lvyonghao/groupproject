package com.comp5348.store;

import com.comp5348.Common.dto.DeliveryRequestDTO;
import com.comp5348.store.dto.*;
import com.comp5348.store.model.Customer;
import com.comp5348.store.model.Goods;
import com.comp5348.store.model.Order;
import com.comp5348.store.service.DeliveryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class OrderServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DeliveryService deliveryService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testSendDeliveryRequest() throws JsonProcessingException {
        // 创建客户对象
        Customer customer = new Customer();
        customer.setId(123L);
        customer.setName("John Doe");
        customer.setPassword("password123");
        customer.setEmail("johndoe@example.com");
        CustomerDTO customerDTO = new CustomerDTO(customer);

        // 创建商品对象
        Goods goods = new Goods();
        goods.setId(123L);
        goods.setName("Laptop");
        goods.setPrice(999.99);
        GoodsDTO goodsDTO = new GoodsDTO(goods);

        // 创建仓库对象
        WarehouseDTO warehouseDTO = new WarehouseDTO();
        warehouseDTO.setId(456L);
        warehouseDTO.setName("Warehouse A");
        warehouseDTO.setLocation("Sydney");

        // 创建 WarehouseGoodsDTO 对象
        WarehouseGoodsDTO warehouseGoodsDTO = new WarehouseGoodsDTO();
        warehouseGoodsDTO.setId(789L);
        warehouseGoodsDTO.setGoods(goodsDTO);
        warehouseGoodsDTO.setWarehouse(warehouseDTO);
        warehouseGoodsDTO.setQuantity(100); // 仓库中商品的数量

        // 创建 OrderWarehouseDTO 对象
        OrderWarehouseDTO orderWarehouseDTO = new OrderWarehouseDTO();
        orderWarehouseDTO.setId(111L);
        orderWarehouseDTO.setWarehouseGoodsDTO(warehouseGoodsDTO);
        orderWarehouseDTO.setQuantity(10); // 此订单中从该仓库取出的商品数量

        // 创建 OrderDTO 对象
        Order order = new Order();
        order.setId(101L);
        order.setGoods(goods);
        order.setCustomer(customer);
        order.setTotalQuantity(10);
        order.setTotalPrice(goods.getPrice() * 10);
        OrderDTO orderDTO = new OrderDTO(order);
//        orderDTO.setOrderWarehouses(List.of(orderWarehouseDTO));

        // 调用被测试方法
        deliveryService.sendDeliveryRequest(orderDTO);

        // 捕获发送给 RabbitMQ 的消息
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("delivery.request.queue"), jsonCaptor.capture());
// 验证发送的 JSON 字符串是否正确
        String capturedJson = jsonCaptor.getValue();
        DeliveryRequestDTO capturedRequest = objectMapper.readValue(capturedJson, DeliveryRequestDTO.class);

// 对消息内容进行断言验证
        assertEquals(orderDTO.getId(), capturedRequest.getOrderId());
        assertEquals(1, capturedRequest.getWarehouseInfos().size());
        assertEquals("Laptop", capturedRequest.getWarehouseInfos().get(0).getGoodsName());
        assertEquals("Warehouse A", capturedRequest.getWarehouseInfos().get(0).getWarehouseName());
        assertEquals("Sydney", capturedRequest.getWarehouseInfos().get(0).getWarehouseAddress());
        assertEquals(10, capturedRequest.getWarehouseInfos().get(0).getQuantity());
    }
}
