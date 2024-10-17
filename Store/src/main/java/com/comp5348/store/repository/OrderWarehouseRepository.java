package com.comp5348.store.repository;

import com.comp5348.store.model.OrderWarehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderWarehouseRepository extends JpaRepository<OrderWarehouse, Long> {
}
