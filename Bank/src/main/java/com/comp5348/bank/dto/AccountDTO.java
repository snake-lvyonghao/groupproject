package com.comp5348.bank.dto;

import com.comp5348.bank.model.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountDTO {
    private long accountId;
    private double balance;
    private String accountOwner;
    private double frozenAmount;
    public AccountDTO(Account accountEntity) {
        this.accountId = accountEntity.getAccountId();
        this.balance = accountEntity.getBalance();
        this.accountOwner = accountEntity.getAccountOwner();
        this.frozenAmount = accountEntity.getFrozenAmount();
    }
}
