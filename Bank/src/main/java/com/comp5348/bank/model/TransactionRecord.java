package com.comp5348.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@Entity
@NoArgsConstructor
public class TransactionRecord {
    @Id
    private long id;
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
}

