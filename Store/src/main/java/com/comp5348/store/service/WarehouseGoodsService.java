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
            // 创建 OrderWarehouse 对象
            OrderWarehouse orderWarehouse = new OrderWarehouse();
            orderWarehouse.setOrder(order);
            orderWarehouse.setWarehouseGoods(warehouseGoods);
            orderWarehouse.setQuantity(allocatedQuantity);

            // 保存 OrderWarehouse 到数据库
            orderWarehouseRepository.save(orderWarehouse);

            // 更新 WarehouseGoods 的数量
            adjustGoodsQuantity(warehouseGoods, allocatedQuantity, false);

            // 更新剩余数量
            remainingQuantity -= allocatedQuantity;

        }

        // 检查是否所有商品数量都已分配完
        if (remainingQuantity > 0) {
            log.warn("Insufficient stock for goods {}", goodsId);
            return false;
        }

        return true;
    }

    public boolean confirmFreezeStock(BusinessActionContext context) {
        // 在 confirm 阶段，这里不需要额外操作，只需返回 true 即可
        return true;
    }

    public boolean cancelFreezeStock(BusinessActionContext context) {
        Integer orderId = (Integer) context.getActionContext("orderId");
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(() -> new RuntimeException("Order not found"));

        //退货到 WarehouseGoods 并删除orderwarehouse
        orderWarehouseRepository.findByOrder(order)
                .forEach(orderWarehouse -> {
                    // 调整库存，退回商品数量
                    adjustGoodsQuantity(
                            orderWarehouse.getWarehouseGoods(),
                            orderWarehouse.getQuantity(),
                            true
                    );
                    // 删除 OrderWarehouse 记录
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
                    // 调整库存，退回商品数量
                    adjustGoodsQuantity(
                            orderWarehouse.getWarehouseGoods(),
                            orderWarehouse.getQuantity(),
                            true
                    );
                });
        return true;
    }

    public boolean confirmRollbackFreezeStock(BusinessActionContext context) {
        // 在 confirm 阶段，删除订单及其相关内容
        Integer orderId = (Integer) context.getActionContext("orderId");
        Order order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(() -> new RuntimeException("Order not found"));
        // Delete orderWarhouse
        orderWarehouseRepository.deleteAll(orderWarehouseRepository.findByOrder(order));
        return true;
    }

    public boolean cancelRollbackFreezeStock(BusinessActionContext context) {
        //业务失败 减少库存
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
        // True 增加库存 False 减少库存
        int newQuantity = increase ? warehouseGoods.getQuantity() + quantity : warehouseGoods.getQuantity() - quantity;

        // 检查库存是否不足
        if (newQuantity < 0) {
            return;
        }

        // 更新库存数量
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
