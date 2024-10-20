package com.comp5348.bank.dto;

import com.comp5348.bank.model.TransactionRecord;
import com.comp5348.bank.model.TransactionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRecordDTO {
    private Long id;
    private String fromAccount;
    private String toAccount;
    private double amount;
    private TransactionStatus status;

    // 从实体类构建DTO
    public TransactionRecordDTO(TransactionRecord transactionRecord) {
        this.id = transactionRecord.getId();
        this.fromAccount = transactionRecord.getFromAccount();
        this.toAccount = transactionRecord.getToAccount();
        this.amount = transactionRecord.getAmount();
        this.status = transactionRecord.getStatus();
    }
}
