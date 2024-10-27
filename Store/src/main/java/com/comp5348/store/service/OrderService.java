package com.comp5348.store.service;


import com.comp5348.Common.model.DeliveryStatus;
import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.exception.OrderRefundException;
import com.comp5348.store.model.*;
import com.comp5348.store.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.comp5348.store.model.Order.OrderStatus.NON_REFUNDABLE;
import static com.comp5348.store.model.Order.OrderStatus.REFUNDABLE;

@Service
@LocalTCC
@Slf4j(topic = "com.comp5348.store")
public class OrderService {

    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseGoodsService warehouseGoodsService;
    private final BankService bankService;
    private final NotificationService notificationService;

    @Autowired
    public OrderService(OrderRepository orderRepository, GoodsRepository goodsRepository, CustomerRepository customerRepository, WarehouseGoodsService warehouseGoodsService, BankService bankService, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
        this.customerRepository = customerRepository;
        this.warehouseGoodsService = warehouseGoodsService;
        this.bankService = bankService;
        this.notificationService = notificationService;
    }

    @GlobalTransactional
    public OrderDTO createOrder(Long goodsId, Long customerId, int quantity) {
        // 查找商品和客户
        Optional<Goods> goodsOptional = goodsRepository.findById(goodsId);
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (goodsOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid goods ID.");
        } else if (customerOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid customer ID.");
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
        order.setStatus(REFUNDABLE);

        // 保存订单到数据库
        order = orderRepository.save(order);

        // 创建上下文对象
        BusinessActionContext actionContext = new BusinessActionContext();

        // 1. 尝试冻结库存
        boolean stockFrozen = warehouseGoodsService.tryFreezeStock(actionContext, goodsId, quantity, order);
        if (!stockFrozen) {
            throw new RuntimeException("Stock freezing failed. Insufficient inventory.");
        }

        // 2. 尝试冻结余额（扣款）
        OrderDTO orderDTO = new OrderDTO(order, true);
        boolean paymentSuccess = bankService.prepareTransaction(actionContext, orderDTO, false);
        if (!paymentSuccess) {
            throw new RuntimeException("Payment freezing failed.");
        }

        // 异步通知DeliveryCO取货
        try {
            notificationService.sendDeliveryRequest(orderDTO);
        } catch (JsonProcessingException e) {
            log.error("Failed to send delivery request for orderId: {}. Error: {}", order.getId(), e.getMessage());
        }

        return new OrderDTO(order, true);
    }



    @GlobalTransactional
    public boolean cancelOrder(Long orderId) {
        // 查找订单
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid order ID.");
        }

        Order order = orderOptional.get();
        switch (order.getStatus()) {
            case CANCELED:
            case NON_REFUNDABLE:
                log.error(String.valueOf(order.getStatus() + "Order Can't be refunded."));
                throw new OrderRefundException("Order Can't be refunded.");
            default:
                break;
        }
        // 获取上下文对象
        BusinessActionContext actionContext = new BusinessActionContext();

        // 1. 回滚冻结的库存并设置订单状态为退单
        boolean stockUnfrozen = warehouseGoodsService.cancelOrder(actionContext, orderId);
        if (!stockUnfrozen) {
            throw new RuntimeException("Stock return failed.");
        }

        // 2. 回滚冻结的余额（退款）
        OrderDTO orderDTO = new OrderDTO(order, true);
        boolean refundSuccess = bankService.prepareTransaction(actionContext, orderDTO, true);
        if (!refundSuccess) {
            throw new RuntimeException("Payment refund failed.");
        }
        //设置order状态为已取消
        setOrderStatus((orderId), Order.OrderStatus.CANCELED);
        // 异步通知Email退款
        try {
            notificationService.sendEmail(orderDTO, DeliveryStatus.CANCELLED);
        } catch (JsonProcessingException e) {
            log.error("Failed to send Email for cancel oreder request for orderId: {}. Error: {}", order.getId(), e.getMessage());
        }
        return true;
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

    //set order can't be canceled
    public void setOrderStatus(Long orderId, Order.OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        order.setStatus(orderStatus);
        orderRepository.save(order); // 保存更新后的订单状态到数据库
    }

    public List<OrderDTO> findRefundableOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdAndStatus(customerId, Order.OrderStatus.REFUNDABLE);
        return orders.stream().map(order -> new OrderDTO(order,true)).toList();
    }
}
