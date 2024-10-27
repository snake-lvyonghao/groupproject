package com.comp5348.bank.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountId;

    @Version
    private int version;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false)
    private String accountOwner; //Account owner name (corresponding to Storeapp)


    @Column(nullable = false)
    private double frozenAmount = 0.0;
}
