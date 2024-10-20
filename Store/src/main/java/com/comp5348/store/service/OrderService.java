package com.comp5348.store.service;


import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.model.*;
import com.comp5348.store.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseGoodsRepository warehouseGoodsRepository;
    private final OrderWarehouseRepository orderWarehouseRepository;


    @Autowired
    public OrderService(OrderRepository orderRepository, GoodsRepository goodsRepository, CustomerRepository customerRepository,  WarehouseGoodsRepository warehouseGoodsRepository, OrderWarehouseRepository orderWarehouseRepository) {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
        this.customerRepository = customerRepository;
        this.warehouseGoodsRepository = warehouseGoodsRepository;
        this.orderWarehouseRepository = orderWarehouseRepository;
    }

    @Transactional
    public OrderDTO createOrder(Long goodsId, Long customerId, int quantity) {
        // 查找商品和客户
        Optional<Goods> goodsOptional = goodsRepository.findById(goodsId);
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (goodsOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid goods or customer ID.");
        } else if (customerOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid goods or customer ID.");
        }

        Goods goods = goodsOptional.get();
        Customer customer = customerOptional.get();

        // 计算订单总价
        double totalPrice = goods.getPrice() * quantity;

        // 创建订单对象
        Order order = new Order();
        order.setGoods(goods);
        order.setCustomer(customer);
        order.setTotalQuantity(quantity);
        order.setTotalPrice(totalPrice);
        // 保存订单到数据库
        order = orderRepository.save(order);

        int remainingQuantity = quantity;
        List<WarehouseGoods> availableWarehouseGoods = warehouseGoodsRepository.findByGoodsId(goodsId);

        for (WarehouseGoods warehouseGoods : availableWarehouseGoods) {
            if (remainingQuantity <= 0) break;

            int availableQuantity = warehouseGoods.getQuantity();
            int allocatedQuantity = Math.min(remainingQuantity, availableQuantity);

            // 创建OrderWarehouse对象
            OrderWarehouse orderWarehouse = new OrderWarehouse();
            orderWarehouse.setOrder(order);
            orderWarehouse.setWarehouseGoods(warehouseGoods);
            orderWarehouse.setQuantity(allocatedQuantity);

            // 保存OrderWarehouse到数据库
            orderWarehouseRepository.save(orderWarehouse);

            // 不更新仓库的商品数量
//            warehouseGoods.setQuantity(availableQuantity - allocatedQuantity);
//            warehouseGoodsRepository.save(warehouseGoods);

            // 更新剩余数量
            remainingQuantity -= allocatedQuantity;
        }

        // 检查是否所有商品数量都已分配完
        if (remainingQuantity > 0) {
            throw new RuntimeException("库存不足，无法满足订单需求。");
        }
        return new OrderDTO(order,true);
    }

    //Refund order
    @Transactional
    public boolean cancelOrder(Long orderId) {
        // 查找订单
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isEmpty()) {
            return false;  // 如果订单不存在，返回 false
        }

        Order order = orderOptional.get();
        //查找合适的仓库 退货到 OrderWarehouse
        List<OrderWarehouse> orderWarehouses = orderWarehouseRepository.findByOrder(order);
        // 逐个恢复库存
        for (OrderWarehouse orderWarehouse : orderWarehouses) {
            int quantityToReturn = orderWarehouse.getQuantity();

            // 查找对应仓库的WarehouseGoods记录
            WarehouseGoods warehouseGoods = orderWarehouse.getWarehouseGoods();
            // 增加库存数量
            warehouseGoods.setQuantity(warehouseGoods.getQuantity() + quantityToReturn);
            warehouseGoodsRepository.save(warehouseGoods);
        }

        // 删除订单的OrderWarehouse记录
        orderWarehouseRepository.deleteAll(orderWarehouses);

        // 删除订单
        orderRepository.delete(order);
        return true;
    }


    public OrderDTO getOrderById(Long orderId) {
        // Retrieve the Order entity by its ID using the repository
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        // If the order is found, convert it to an OrderDTO and return it
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            return new OrderDTO(order, true); // Assuming you want related entities included
        }

        // If the order is not found, throw an exception or handle it as needed
        throw new RuntimeException("Order not found with ID: " + orderId);
    }

    //获取实体
    public Order getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

}
