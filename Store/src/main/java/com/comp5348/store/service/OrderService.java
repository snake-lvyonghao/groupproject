package com.comp5348.store.service;


import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.model.*;
import com.comp5348.store.repository.*;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@LocalTCC
public class OrderService {

    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseGoodsService warehouseGoodsService;
    private final BankService bankService;

    @Autowired
    public OrderService(OrderRepository orderRepository, GoodsRepository goodsRepository, CustomerRepository customerRepository, WarehouseGoodsService warehouseGoodsService, BankService bankService) {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
        this.customerRepository = customerRepository;
        this.warehouseGoodsService = warehouseGoodsService;
        this.bankService = bankService;
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

        //TODO 通知DeliveryCO取货

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

        // 获取上下文对象
        BusinessActionContext actionContext = new BusinessActionContext();

        // 1. 回滚冻结的库存
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

        //TODO Email 通知客户
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
