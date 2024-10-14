package com.comp5348.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Goods {
    @Id
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;
}
