package com.comp5348.store.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Column(nullable = false)
    private int version;

    @ManyToOne
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;  // 一个订单只会涉及一种商品

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;  // 每个订单对应一个客户

    @Column(nullable = false)
    private int totalQuantity;  // 商品的总数量

    @Column(nullable = false)
    private double totalPrice;  // 总价格
}
