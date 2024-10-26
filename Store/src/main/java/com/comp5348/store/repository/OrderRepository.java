package com.comp5348.store.repository;

import com.comp5348.store.model.Customer;
import com.comp5348.store.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
    List<Order> findByCustomerIdAndStatus(Long customerId, Order.OrderStatus status);
}
