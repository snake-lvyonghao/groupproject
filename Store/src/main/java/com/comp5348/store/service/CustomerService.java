package com.comp5348.store.service;

import com.comp5348.store.model.Customer;

public class CustomerService {
    public void registerCustomer(Customer customer, String rawPassword) {
        customer.encryptAndSetPassword(rawPassword);  // 加密并设置密码
        // 保存 customer 对象到数据库
    }

    public boolean login(String rawPassword, Customer customer) {
        // 检查密码是否匹配
        return customer.checkPassword(rawPassword);
    }
}
