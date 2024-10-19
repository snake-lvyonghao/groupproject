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
    private Long transactionId;  // 交易的唯一标识符
    private Long accountId;  // 相关联的账户ID
    private double amount;  // 交易的金额
    private TransactionStatus status;  // 交易的状态

    public TransactionRecordDTO(TransactionRecord transactionRecord) {
        this.transactionId = transactionRecord.getTransactionId();
        this.accountId = transactionRecord.getAccountId();
        this.amount = transactionRecord.getAmount();
        this.status = transactionRecord.getStatus();
    }
}
