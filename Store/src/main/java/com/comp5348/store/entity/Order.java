package com.comp5348.store.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    private int version;

    @ManyToOne
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;  // 一个订单只会涉及一种商品

    @Column(nullable = false)
    private int totalQuantity;  // 商品的总数量

    @Column(nullable = false)
    private double totalPrice;  // 总价格

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderWarehouse> orderWarehouses;  // 保存订单涉及的仓库信息
}
