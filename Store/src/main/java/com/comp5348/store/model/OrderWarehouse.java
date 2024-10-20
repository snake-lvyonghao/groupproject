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
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;  // 关联的订单

    @ManyToOne
    @JoinColumn(name = "warehousegoods_id", nullable = false)
    private WarehouseGoods warehouseGoods;  // 发货的仓库

    @Column(nullable = false)
    private int quantity;  // 从该仓库发货的商品数量
}
