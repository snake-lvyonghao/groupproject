package com.comp5348.store.dto;
import com.comp5348.store.model.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrderDTO {
    private long id;  // 订单ID
    private GoodsDTO goods;  // 订单中的商品信息
    private CustomerDTO customer;  // 客户信息
    private int totalQuantity;  // 商品的总数量
    private double totalPrice;  // 总价格

    /**
     * Constructs an OrderDTO from an Order entity.
     *
     * @param orderEntity the order entity
     * @param includeRelatedEntities whether to include related entities
     */
    public OrderDTO(Order orderEntity, boolean includeRelatedEntities) {
        this.id = orderEntity.getId();
        this.totalQuantity = orderEntity.getTotalQuantity();
        this.totalPrice = orderEntity.getTotalPrice();

        if (includeRelatedEntities) {
            this.goods = new GoodsDTO(orderEntity.getGoods());
            this.customer = new CustomerDTO(orderEntity.getCustomer());
        }
    }

    /**
     * Constructs an OrderDTO from an Order entity.
     *
     * @param orderEntity the order entity
     */
    public OrderDTO(Order orderEntity) {
        this(orderEntity, false);
    }
}
