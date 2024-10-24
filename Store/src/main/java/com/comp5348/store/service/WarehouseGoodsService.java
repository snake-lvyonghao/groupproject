package com.comp5348.store.service;

import com.comp5348.store.dto.OrderDTO;
import com.comp5348.store.dto.WarehouseGoodsDTO;
import com.comp5348.store.model.*;
import com.comp5348.store.repository.*;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
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

    @Transactional
    @TwoPhaseBusinessAction(name = "freezeStockAction", commitMethod = "confirmFreezeStock", rollbackMethod = "cancelFreezeStock")
    public boolean tryFreezeStock(BusinessActionContext context, Long goodsId, int remainingQuantity, Order order){
        List<WarehouseGoods> availableWarehouseGoods = findByGoodsId(goodsId);
        // 把事务 ID 保存到上下文中以便于 commit 和 rollback 阶段使用
        context.addActionContext("orderId", order.getId());
        for (WarehouseGoods warehouseGoods : availableWarehouseGoods) {
            if (remainingQuantity <= 0) break;

            int availableQuantity = warehouseGoods.getQuantity();
            int allocatedQuantity = Math.min(remainingQuantity, availableQuantity);

            // 创建OrderWarehouse对象
            OrderWarehouse orderWarehouse = new OrderWarehouse();
            orderWarehouse.setOrder(order);
            orderWarehouse.setWarehouseGoods(warehouseGoods);
            orderWarehouse.setQuantity(allocatedQuantity);

            // 保存OrderWarehouse到数据库
            orderWarehouseRepository.save(orderWarehouse);

            // 更新剩余数量
            remainingQuantity -= allocatedQuantity;
        }
        // 检查是否所有商品数量都已分配完
        if (remainingQuantity > 0) {
            return false; // 库存不足，返回 false 表示冻结失败
        }
        // 更新仓库的商品数量
        orderWarehouseRepository.findByOrder(order)
                .forEach(orderWarehouse ->
                        adjustGoodsQuantity(
                                orderWarehouse.getWarehouseGoods(),
                                orderWarehouse.getQuantity(),
                                false
                        )
                );
        // 检查是否所有商品数量都已分配完
        return true;
    }
    @Transactional
    public boolean confirmFreezeStock(BusinessActionContext context) {
        // 在 confirm 阶段，这里不需要额外操作，只需返回 true 即可
        return true;
    }
    @Transactional
    public boolean cancelFreezeStock(BusinessActionContext context) {
        Long orderId = (Long) context.getActionContext("orderId");
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        //退货到 OrderWarehouse
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
        return true;
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
