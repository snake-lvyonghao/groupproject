package com.comp5348.store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Version
    private int version;
    private String fromAccount;
    private String toAccount;
    private double amount;

    private TransactionStatus status;
}
