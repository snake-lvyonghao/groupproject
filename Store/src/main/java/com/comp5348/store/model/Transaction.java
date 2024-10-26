package com.comp5348.store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date date;

    @PrePersist
    protected void onCreate() {
        date = new Date();
    }

    public enum TransactionStatus {
        OPEN,
        COMMITTED,
        SUCCESS,
        FAILED
    }

}
