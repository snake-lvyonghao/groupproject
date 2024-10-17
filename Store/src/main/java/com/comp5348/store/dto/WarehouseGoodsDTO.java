package com.comp5348.store.dto;

import com.comp5348.store.model.WarehouseGoods;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WarehouseGoodsDTO {
    private Long id;
    private WarehouseDTO warehouse;
    private GoodsDTO goods;
    private int quantity;

    /**
     * Constructs a WarehouseGoodsDTO from a WarehouseGoods entity.
     *
     * @param warehouseGoodsEntity the WarehouseGoods entity
     */
    public WarehouseGoodsDTO(WarehouseGoods warehouseGoodsEntity) {
        this(warehouseGoodsEntity, false);
    }

    /**
     * Constructs a WarehouseGoodsDTO from a WarehouseGoods entity,
     * optionally including related entities.
     *
     * @param warehouseGoodsEntity    the WarehouseGoods entity
     * @param includeRelatedEntities  whether to include related entities
     */
    public WarehouseGoodsDTO(WarehouseGoods warehouseGoodsEntity, boolean includeRelatedEntities) {
        this.id = warehouseGoodsEntity.getId();
        this.quantity = warehouseGoodsEntity.getQuantity();

        if (includeRelatedEntities) {
            this.warehouse = new WarehouseDTO(warehouseGoodsEntity.getWarehouse());
            this.goods = new GoodsDTO(warehouseGoodsEntity.getGoods());
        }
    }
}
