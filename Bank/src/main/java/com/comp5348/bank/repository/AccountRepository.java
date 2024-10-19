package com.comp5348.bank.repository;

import com.comp5348.bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByAccountOwner(String accountOwner);
}
