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
    public CustomerDTO registerCustomer(String name, String rawPassword,String email) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.encryptAndSetPassword(rawPassword);
        customer.setEmail(email);
        customerRepository.save(customer);
        return new CustomerDTO(customer);
    }


    public CustomerDTO getCustomerById(long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        return customer.map(CustomerDTO::new).orElse(null);
    }


    public boolean authenticateCustomer(String email, String rawPassword) {
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(email);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            return customer.checkPassword(rawPassword); // 验证密码
        }
        return false;
    }


    public Customer updatePassword(long customerId, String newPassword) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.encryptAndSetPassword(newPassword);
            return customerRepository.save(customer);
        } else {
            throw new RuntimeException("Customer not found");
        }
    }
}
