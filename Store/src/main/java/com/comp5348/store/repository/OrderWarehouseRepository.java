package com.comp5348.store.repository;

import com.comp5348.store.model.Order;
import com.comp5348.store.model.OrderWarehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderWarehouseRepository extends JpaRepository<OrderWarehouse, Long> {
    Collection<Object> findByOrderId(Long orderId);

    List<OrderWarehouse> findByOrder(Order order);
}
