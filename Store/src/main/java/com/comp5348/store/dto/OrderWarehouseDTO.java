package com.comp5348.store.dto;

import com.comp5348.store.model.OrderWarehouse;
import com.comp5348.store.model.WarehouseGoods;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class OrderWarehouseDTO {
    private Long id;
    private OrderDTO order;
    private WarehouseGoodsDTO warehouseGoodsDTO;
    private int quantity;

    /**
     * Constructs an OrderWarehouseDTO from an OrderWarehouse entity.
     *
     * @param orderWarehouseEntity the entity object
     */
    public OrderWarehouseDTO(OrderWarehouse orderWarehouseEntity) {
        this(orderWarehouseEntity, false);
    }

    /**
     * Constructs an OrderWarehouseDTO from an OrderWarehouse entity,
     * optionally including related entities.
     *
     * @param orderWarehouseEntity     the entity object
     * @param includeRelatedEntities   whether to include related entities
     */
    public OrderWarehouseDTO(OrderWarehouse orderWarehouseEntity, boolean includeRelatedEntities) {
        this.id = orderWarehouseEntity.getId();
        this.quantity = orderWarehouseEntity.getQuantity();
        if (includeRelatedEntities) {
            this.order = new OrderDTO(orderWarehouseEntity.getOrder());
            this.warehouseGoodsDTO = new WarehouseGoodsDTO(orderWarehouseEntity.getWarehouseGoods());
        }
    }
}
