package com.comp5348.store.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@Entity
public class OrderWarehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "warehousegoods_id", nullable = false)
    private WarehouseGoods warehouseGoods;
    @Column(nullable = false)
    private int quantity;  // Quantity of goods shipped from the warehouse
}
