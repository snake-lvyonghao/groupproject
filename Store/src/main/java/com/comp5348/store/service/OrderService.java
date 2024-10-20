package com.comp5348.store.service;


import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.model.*;
import com.comp5348.store.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseGoodsService warehouseGoodsService;
    private final OrderWarehouseRepository orderWarehouseRepository;
    private final BankService bankService;


    @Autowired
    public OrderService(OrderRepository orderRepository, GoodsRepository goodsRepository, CustomerRepository customerRepository, WarehouseGoodsService warehouseGoodsService, OrderWarehouseRepository orderWarehouseRepository, BankService bankService) {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
        this.customerRepository = customerRepository;
        this.warehouseGoodsService = warehouseGoodsService;
        this.orderWarehouseRepository = orderWarehouseRepository;
        this.bankService = bankService;
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
        List<WarehouseGoods> availableWarehouseGoods = warehouseGoodsService.findByGoodsId(goodsId);

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
            
            // 更新剩余数量
            remainingQuantity -= allocatedQuantity;
        }

        // 检查是否所有商品数量都已分配完
        if (remainingQuantity > 0) {
            throw new RuntimeException("The inventory is insufficient to meet the order demand.");
        }

        //Grpc process payment
        OrderDTO orderDTO = new OrderDTO(order,true);
        boolean paymentSuccess = bankService.processTransaction(orderDTO,false);
        if (!paymentSuccess) {
            throw new RuntimeException("Payment failed");
        }

        // 更新仓库的商品数量
        orderWarehouseRepository.findByOrder(order)
                .forEach(orderWarehouse ->
                        warehouseGoodsService.adjustGoodsQuantity(
                                orderWarehouse.getWarehouseGoods(),
                                orderWarehouse.getQuantity(),
                                true
                        )
                );


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
        OrderDTO orderDTO = new OrderDTO(order,true);
        boolean paymentSuccess = bankService.processTransaction(orderDTO,false);
        if (!paymentSuccess) {
            throw new RuntimeException("Payment failed");
        }
        //TODO Email notify customer
        //退货到 OrderWarehouse
        orderWarehouseRepository.findByOrder(order)
                .forEach(orderWarehouse -> {
                    // 调整库存，退回商品数量
                    warehouseGoodsService.adjustGoodsQuantity(
                            orderWarehouse.getWarehouseGoods(),
                            orderWarehouse.getQuantity(),
                            true
                    );
                    // 删除 OrderWarehouse 记录
                    orderWarehouseRepository.delete(orderWarehouse);
                });

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

    @Transactional
    public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
        // 查找客户
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isEmpty()) {
            throw new RuntimeException("Customer not found with ID: " + customerId);
        }

        Customer customer = customerOptional.get();

        // 查找该客户的所有订单
        List<Order> orders = orderRepository.findByCustomer(customer);

        // 将订单实体转换为 DTO 并返回
        return orders.stream()
                .map(order -> new OrderDTO(order, true)) // true 表示包含关联的实体
                .collect(Collectors.toList());
    }

}
