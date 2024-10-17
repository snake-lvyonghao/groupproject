package com.comp5348.store.repository;

import com.comp5348.store.model.WarehouseGoods;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseGoodsRepository extends JpaRepository<WarehouseGoods, Long> {
}
