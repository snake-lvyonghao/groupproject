package com.comp5348.bank.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountId;  // 用户的账户ID

    @Version
    private int version;

    @Column(nullable = false)
    private double balance;  // 账户余额

    @Column(nullable = false)
    private String accountOwner; //账户拥有者名字（对应Storeapp）

    @Column(nullable = false)
    private double frozenAmount = 0.0;  // 被冻结的金额
}
