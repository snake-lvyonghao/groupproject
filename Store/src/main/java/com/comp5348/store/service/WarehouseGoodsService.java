package com.comp5348.store.service;

import com.comp5348.store.dto.WarehouseGoodsDTO;
import com.comp5348.store.model.*;
import com.comp5348.store.repository.*;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "com.comp5348.store")
@LocalTCC
public class WarehouseGoodsService {

    private final WarehouseGoodsRepository warehouseGoodsRepository;
    private final GoodsRepository goodsRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderRepository orderRepository;
    private final OrderWarehouseRepository orderWarehouseRepository;

    @Autowired
    public WarehouseGoodsService(WarehouseGoodsRepository warehouseGoodsRepository,
                                 GoodsRepository goodsRepository,
                                 WarehouseRepository warehouseRepository, OrderRepository orderRepository, OrderWarehouseRepository orderWarehouseRepository) {
        this.warehouseGoodsRepository = warehouseGoodsRepository;
        this.goodsRepository = goodsRepository;
        this.warehouseRepository = warehouseRepository;
        this.orderRepository = orderRepository;
        this.orderWarehouseRepository = orderWarehouseRepository;
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


    @TwoPhaseBusinessAction(name = "freezeStockAction", commitMethod = "confirmFreezeStock", rollbackMethod = "cancelFreezeStock")
    public boolean tryFreezeStock(BusinessActionContext context, Long goodsId, int remainingQuantity, Order order) {
        List<WarehouseGoods> availableWarehouseGoods = findByGoodsId(goodsId);
        context.addActionContext("orderId", order.getId());

        for (WarehouseGoods warehouseGoods : availableWarehouseGoods) {
            if (remainingQuantity <= 0) break;
            if (warehouseGoods == null || warehouseGoods.getWarehouse() == null) {
                log.error("WarehouseGoods or its warehouse is null. Goods ID: {}", goodsId);
                return false;
            }
            int availableQuantity = warehouseGoods.getQuantity();
            if (availableQuantity <= 0) {
                continue;
            }
            int allocatedQuantity = Math.min(remainingQuantity, availableQuantity);
            log.info("Allocated {} items of goods {} from warehouse {}", allocatedQuantity, goodsId, warehouseGoods.getWarehouse().getId());
            // creat OrderWarehouse object
            OrderWarehouse orderWarehouse = new OrderWarehouse();
            orderWarehouse.setOrder(order);
            orderWarehouse.setWarehouseGoods(warehouseGoods);
            orderWarehouse.setQuantity(allocatedQuantity);

            // save
            orderWarehouseRepository.save(orderWarehouse);

            // Update the quantity of WarehouseGoods
            adjustGoodsQuantity(warehouseGoods, allocatedQuantity, false);

            // Update remaining quantity
            remainingQuantity -= allocatedQuantity;

        }

        // Check that all quantities of goods have been allocated
        if (remainingQuantity > 0) {
            log.warn("Insufficient stock for goods {}", goodsId);
            return false;
        }

        return true;
    }

    public boolean confirmFreezeStock(BusinessActionContext context) {
        return true;
    }

    public boolean cancelFreezeStock(BusinessActionContext context) {
        Integer orderId = (Integer) context.getActionContext("orderId");
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(() -> new RuntimeException("Order not found"));

        // Return to WarehouseGoods and delete orderwarehouse
        orderWarehouseRepository.findByOrder(order)
                .forEach(orderWarehouse -> {
                    // Adjust inventory and return quantity of goods
                    adjustGoodsQuantity(
                            orderWarehouse.getWarehouseGoods(),
                            orderWarehouse.getQuantity(),
                            true
                    );
                    // Delete the OrderWarehouse record
                    orderWarehouseRepository.delete(orderWarehouse);
                });
        orderRepository.delete(order);
        return true;
    }

    @TwoPhaseBusinessAction(name = "cancelOrder", commitMethod = "confirmRollbackFreezeStock", rollbackMethod = "cancelRollbackFreezeStock")
    public boolean cancelOrder(BusinessActionContext context,Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        context.addActionContext("orderId", orderId);
        orderWarehouseRepository.findByOrder(order)
                .forEach(orderWarehouse -> {
                    // Adjust inventory and return quantity of goods
                    adjustGoodsQuantity(
                            orderWarehouse.getWarehouseGoods(),
                            orderWarehouse.getQuantity(),
                            true
                    );
                });
        return true;
    }

    public boolean confirmRollbackFreezeStock(BusinessActionContext context) {
        // In the confirm phase, delete the order and its related content
        Integer orderId = (Integer) context.getActionContext("orderId");
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(() -> new RuntimeException("Order not found"));
        // Delete orderWarhouse
        orderWarehouseRepository.deleteAll(orderWarehouseRepository.findByOrder(order));
        return true;
    }

    public boolean cancelRollbackFreezeStock(BusinessActionContext context) {
        // Business failure reduces inventory
        Integer orderId = (Integer) context.getActionContext("orderId");
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(() -> new RuntimeException("Order not found"));
        orderWarehouseRepository.findByOrder(order)
                .forEach(orderWarehouse -> {
                    // 调整库存，退回商品数量
                    adjustGoodsQuantity(
                            orderWarehouse.getWarehouseGoods(),
                            orderWarehouse.getQuantity(),
                            false
                    );
                });
        return true;
    }

    public void adjustGoodsQuantity(WarehouseGoods warehouseGoods, int quantity, boolean increase) {
        // True increases inventory False Reduces inventory
        int newQuantity = increase ? warehouseGoods.getQuantity() + quantity : warehouseGoods.getQuantity() - quantity;

        // Check for insufficient stock
        if (newQuantity < 0) {
            return;
        }

        // update stock
        warehouseGoods.setQuantity(newQuantity);
        warehouseGoodsRepository.save(warehouseGoods);
    }


    @Transactional
    public void deleteWarehouseGoods(Long warehouseGoodsId) {
        warehouseGoodsRepository.deleteById(warehouseGoodsId);
    }

    public List<WarehouseGoods> findByGoodsId(Long goodsId) {
        return warehouseGoodsRepository.findByGoodsId(goodsId);
    }
}
