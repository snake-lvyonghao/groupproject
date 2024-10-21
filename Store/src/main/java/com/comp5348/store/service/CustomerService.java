package com.comp5348.store.service;

import com.comp5348.store.dto.CustomerDTO;
import com.comp5348.store.model.Customer;
import com.comp5348.store.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;


    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // 注册新用户，保存到数据库中
    public CustomerDTO registerCustomer(String name, String rawPassword) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.encryptAndSetPassword(rawPassword); // 使用加密密码
        Customer saveCustomer = customerRepository.save(customer);  // 保存用户到数据库
        return new CustomerDTO(saveCustomer);
    }

    // 根据 ID 查找用户
    public CustomerDTO getCustomerById(long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        return customer.map(CustomerDTO::new).orElse(null);
    }

    // 登录验证用户
    public boolean authenticateCustomer(String name, String rawPassword) {
        Optional<Customer> optionalCustomer = customerRepository.findByName(name);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            return customer.checkPassword(rawPassword); // 验证密码
        }
        return false;
    }

    // 更新用户密码
    public Customer updatePassword(long customerId, String newPassword) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.encryptAndSetPassword(newPassword);  // 加密新密码
            return customerRepository.save(customer);  // 更新保存用户
        } else {
            throw new RuntimeException("Customer not found");
        }
    }
}
