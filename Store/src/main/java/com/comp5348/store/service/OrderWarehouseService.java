package com.comp5348.store.service;

import com.comp5348.store.dto.OrderWarehouseDTO;
import com.comp5348.store.model.Order;
import com.comp5348.store.model.OrderWarehouse;
import com.comp5348.store.model.WarehouseGoods;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.repository.OrderWarehouseRepository;
import com.comp5348.store.repository.WarehouseGoodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class OrderWarehouseService {
    private final OrderWarehouseRepository orderWarehouseRepository;
    private final OrderRepository orderRepository;
    private final WarehouseGoodsRepository warehouseGoodsRepository;

    @Autowired
    public OrderWarehouseService(OrderWarehouseRepository orderWarehouseRepository, OrderRepository orderRepository, WarehouseGoodsRepository warehouseGoodsRepository) {
        this.orderWarehouseRepository = orderWarehouseRepository;
        this.orderRepository = orderRepository;
        this.warehouseGoodsRepository = warehouseGoodsRepository;
    }


    public List<OrderWarehouseDTO> getOrderWarehousesByOrderId(Long orderId) {
        return orderWarehouseRepository.findByOrderId(orderId).stream()
                .map(orderWarehouse -> new OrderWarehouseDTO((OrderWarehouse) orderWarehouse, true))
                .collect(Collectors.toList());
    }

    public OrderWarehouseDTO creatOrderWarehouse(Long orderId,Long warehouseGoodsId,int quantity){
        OrderWarehouse orderWarehouse = new OrderWarehouse();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        WarehouseGoods warehousegoods = warehouseGoodsRepository.findById(warehouseGoodsId)
                .orElseThrow(() -> new RuntimeException("WarehouseGoods not found with ID: " + warehouseGoodsId));
        // Retrieve the warehouse using the warehouse ID
        orderWarehouse.setOrder(order);
        orderWarehouse.setQuantity(quantity);
        orderWarehouse.setWarehouseGoods(warehousegoods);
        // Save the OrderWarehouse entity to the database
        orderWarehouse = orderWarehouseRepository.save(orderWarehouse);
        return  new OrderWarehouseDTO(orderWarehouse, true);
    }
}
