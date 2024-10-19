package com.comp5348.store.service;

import com.comp5348.grpc.BankServiceGrpc;
import com.comp5348.grpc.PrepareRequest;
import com.comp5348.grpc.PrepareResponse;
import com.comp5348.store.model.Order;
import com.comp5348.store.model.Goods;
import com.comp5348.store.model.Customer;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.repository.GoodsRepository;
import com.comp5348.store.repository.CustomerRepository;

import jakarta.transaction.Transactional;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.comp5348.grpc.PrepareRequest.Action.ADD_BALANCE;
import static com.comp5348.grpc.PrepareRequest.Action.REDUCE_BALANCE;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseService warehouseService;

    @GrpcClient("bank")
    private BankServiceGrpc.BankServiceFutureStub bankStub;

    @Autowired
    public OrderService(OrderRepository orderRepository, GoodsRepository goodsRepository, CustomerRepository customerRepository, WarehouseService warehouseService) {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
        this.customerRepository = customerRepository;
        this.warehouseService = warehouseService;
    }

    @Transactional
    public boolean createOrder(Long goodsId, Long customerId, int quantity) {
        // 查找商品和客户
        Optional<Goods> goodsOptional = goodsRepository.findById(goodsId);
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (goodsOptional.isEmpty() || customerOptional.isEmpty()) {
            return false;
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
        //TODO 查找合适的仓库 存到 OrderWarehouse

        // 保存订单到数据库
        order = orderRepository.save(order);

        // 执行银行扣款操作
        Future<PrepareResponse> bankResponseFuture = bankStub.prepare(PrepareRequest.newBuilder()
                .setCusomerName(order.getCustomer().getName())
                .setMoney(totalPrice)
                .setAction(REDUCE_BALANCE)
                .setTransactionId(order.getId())
                .build());

        PrepareResponse bankResponse;
        try {
            bankResponse = bankResponseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return bankResponse.getSuccess();
    }

    //Refund order
    @Transactional
    public boolean refundOrder(Long orderId) {
        // 查找订单
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isEmpty()) {
            return false;  // 如果订单不存在，返回 false
        }

        Order order = orderOptional.get();
        //TODO 查找合适的仓库 退货到 OrderWarehouse
        double refundAmount = order.getTotalPrice();  // 获取订单的总金额

        // 调用银行服务进行退款操作
        Future<PrepareResponse> bankResponseFuture = bankStub.prepare(PrepareRequest.newBuilder()
                .setCusomerName(order.getCustomer().getName())
                .setMoney(refundAmount)
                .setAction(ADD_BALANCE)  // 退款操作
                .setTransactionId(order.getId())
                .build());

        PrepareResponse bankResponse;
        try {
            // 等待银行的响应
            bankResponse = bankResponseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error processing refund", e);
        }
        return bankResponse.getSuccess();
    }


}
