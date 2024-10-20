package com.comp5348.store.repository;

import com.comp5348.store.model.Goods;
import com.comp5348.store.model.Warehouse;
import com.comp5348.store.model.WarehouseGoods;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseGoodsRepository extends JpaRepository<WarehouseGoods, Long> {
    Optional<WarehouseGoods> findByWarehouseAndGoods(Warehouse warehouse, Goods goods);

    Optional<WarehouseGoods> findByWarehouseIdAndGoodsId(Long warehouseId, Long goodsId);


    List<WarehouseGoods> findByGoodsId(Long goodsId);
}
