package com.comp5348.store.dto;

import com.comp5348.store.model.Goods;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GoodsDTO {
    private long id;

    private String name;

    private double price;

    public GoodsDTO(Goods goodsEntity)
    {
        this.id = goodsEntity.getId();
        this.name = goodsEntity.getName();
        this.price = goodsEntity.getPrice();
    }
}
