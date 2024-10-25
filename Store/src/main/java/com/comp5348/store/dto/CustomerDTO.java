package com.comp5348.store.dto;

import com.comp5348.store.model.Customer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CustomerDTO {
    private long id;
    private String name;
    private String password;
    private String email;

    public CustomerDTO(Customer customerEntity){
        this.id = customerEntity.getId();
        this.name = customerEntity.getName();
        this.password = customerEntity.getPassword();
        this.email = customerEntity.getEmail();
    }

}
