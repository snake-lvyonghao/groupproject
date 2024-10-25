package com.comp5348.bank.dto;

import com.comp5348.bank.model.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountDTO {
    private long accountId;  // 用户的账户ID
    private double balance;  // 账户余额
    private String accountOwner; // 账户拥有者
    private double frozenAmount;  // 被冻结的金额
    public AccountDTO(Account accountEntity) {
        this.accountId = accountEntity.getAccountId();
        this.balance = accountEntity.getBalance();
        this.accountOwner = accountEntity.getAccountOwner();
        this.frozenAmount = accountEntity.getFrozenAmount();
    }
}
