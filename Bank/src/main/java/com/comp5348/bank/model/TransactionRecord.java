package com.comp5348.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}

