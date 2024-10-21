package com.comp5348.store.service;

import com.comp5348.store.dto.WarehouseGoodsDTO;
import com.comp5348.store.model.Goods;
import com.comp5348.store.model.Warehouse;
import com.comp5348.store.model.WarehouseGoods;
import com.comp5348.store.repository.WarehouseGoodsRepository;
import com.comp5348.store.repository.GoodsRepository;
import com.comp5348.store.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WarehouseGoodsService {

    private final WarehouseGoodsRepository warehouseGoodsRepository;
    private final GoodsRepository goodsRepository;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public WarehouseGoodsService(WarehouseGoodsRepository warehouseGoodsRepository,
                                 GoodsRepository goodsRepository,
                                 WarehouseRepository warehouseRepository) {
        this.warehouseGoodsRepository = warehouseGoodsRepository;
        this.goodsRepository = goodsRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public WarehouseGoodsDTO createOrUpdateWarehouseGoods(Long warehouseId, Long goodsId, int quantity) {
        try {
            // Fetch warehouse and goods entities
            Optional<Warehouse> warehouse = warehouseRepository.findById(warehouseId);
            Optional<Goods> goods = goodsRepository.findById(goodsId);

            if (warehouse.isEmpty() || goods.isEmpty()) {
                throw new RuntimeException("Warehouse or Goods not found.");
            }

            // Check if a record already exists for this warehouse and goods
            WarehouseGoods warehouseGoods = warehouseGoodsRepository
                    .findByWarehouseAndGoods(warehouse.get(), goods.get())
                    .orElse(new WarehouseGoods());

            // Set properties
            warehouseGoods.setWarehouse(warehouse.get());
            warehouseGoods.setGoods(goods.get());
            warehouseGoods.setQuantity(quantity);

            // Save the record
            WarehouseGoods savedWarehouseGoods = warehouseGoodsRepository.save(warehouseGoods);
            return new WarehouseGoodsDTO(savedWarehouseGoods, true);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create or update warehouse goods.", e);
        }
    }

    @Transactional
    public boolean adjustGoodsQuantity(WarehouseGoods warehouseGoods, int quantity, boolean increase) {
        // 如果是增加库存，直接增加数量，否则减少数量
        int newQuantity = increase ? warehouseGoods.getQuantity() + quantity : warehouseGoods.getQuantity() - quantity;

        // 检查库存是否不足
        if (newQuantity < 0) {
            return false;
        }

        // 更新库存数量
        warehouseGoods.setQuantity(newQuantity);
        warehouseGoodsRepository.save(warehouseGoods);
        return true;
    }


    @Transactional
    public void deleteWarehouseGoods(Long warehouseGoodsId) {
        warehouseGoodsRepository.deleteById(warehouseGoodsId);
    }

    public List<WarehouseGoods> findByGoodsId(Long goodsId) {
        return warehouseGoodsRepository.findByGoodsId(goodsId);
    }
}
