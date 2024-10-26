package com.comp5348.store.model;
import java.util.Date;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date date;

    @PrePersist
    protected void onCreate() {
        date = new Date();
    }

    public enum OrderStatus {
        REFUNDABLE, // 可退单
        NON_REFUNDABLE, // 不可退单
        CANCELED
    }
}
