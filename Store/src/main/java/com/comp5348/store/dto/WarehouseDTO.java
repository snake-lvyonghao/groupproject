package com.comp5348.store.dto;

import com.comp5348.store.model.Warehouse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WarehouseDTO {
    private long id;
    private String name;

    /**
     * Constructs a WarehouseDTO from a Warehouse entity.
     *
     * @param warehouseEntity the Warehouse entity
     */
    public WarehouseDTO(Warehouse warehouseEntity) {
        this(warehouseEntity, false);
    }

    public WarehouseDTO(Warehouse warehouse, boolean b) {
        this.id = warehouse.getId();
        this.name = warehouse.getName();
    }
}
