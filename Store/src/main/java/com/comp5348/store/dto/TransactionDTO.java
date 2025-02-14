package com.comp5348.store.dto;

import com.comp5348.store.model.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDTO {
    private long id;
    private String fromAccount;
    private String toAccount;
    private double amount;
    private Transaction.TransactionStatus status;
    private Date date;

    // 从实体类构建DTO
    public TransactionDTO(Transaction transactionEntity) {
        this.id = transactionEntity.getId();
        this.fromAccount = transactionEntity.getFromAccount();
        this.toAccount = transactionEntity.getToAccount();
        this.amount = transactionEntity.getAmount();
        this.status = transactionEntity.getStatus();
        this.date = transactionEntity.getDate();
    }
}
