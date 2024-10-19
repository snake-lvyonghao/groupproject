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
    public WarehouseGoodsDTO adjustGoodsQuantity(Long warehouseId, Long goodsId, int quantity, boolean increase) {
        Optional<WarehouseGoods> warehouseGoodsDTO = warehouseGoodsRepository.findByWarehouseIdAndGoodsId(warehouseId, goodsId);
        WarehouseGoods warehouseGoods;
        if (warehouseGoodsDTO.isEmpty()) {
            throw new RuntimeException("Warehouse or Goods not found.");
        }
        warehouseGoods = warehouseGoodsDTO.get();
        if (increase) {
            warehouseGoods.setQuantity(warehouseGoods.getQuantity() - quantity);
        } else {
            int newQuantity = warehouseGoods.getQuantity() + quantity;
            if (newQuantity < 0) {
                throw new RuntimeException("Insufficient stock.");
            }
            warehouseGoods.setQuantity(newQuantity);
        }

        // Save the updated or new WarehouseGoods entity
        WarehouseGoods savedWarehouseGoods = warehouseGoodsRepository.save(warehouseGoods);
        return new WarehouseGoodsDTO(savedWarehouseGoods);

    }

    public void deleteWarehouseGoods(Long warehouseGoodsId) {
        warehouseGoodsRepository.deleteById(warehouseGoodsId);
    }
}
