package com.comp5348.store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;
}
