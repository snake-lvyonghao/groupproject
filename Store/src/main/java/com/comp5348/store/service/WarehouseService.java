package com.comp5348.store.service;

import com.comp5348.store.dto.WarehouseDTO;
import com.comp5348.store.model.Warehouse;
import com.comp5348.store.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public WarehouseDTO getWarehouseById(Long id) {
        Optional<Warehouse> warehouse = warehouseRepository.findById(id);
        return warehouse.map(WarehouseDTO::new).orElse(null);
    }

    public WarehouseDTO saveWarehouse(WarehouseDTO warehouseDTO) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(warehouseDTO.getName());
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return new WarehouseDTO(savedWarehouse);
    }
}
