package com.comp5348.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@Entity
@NoArgsConstructor
public class TransactionRecord {
    private Long accountId;
    @Id
    private Long transactionId;

    @Column
    private double amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

}

