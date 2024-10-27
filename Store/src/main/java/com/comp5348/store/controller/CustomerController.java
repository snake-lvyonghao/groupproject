package com.comp5348.store.controller;

import com.comp5348.store.dto.CustomerDTO;
import com.comp5348.store.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        boolean isAuthenticated = customerService.authenticateCustomer(loginRequest.Username, loginRequest.Password);
        if (isAuthenticated) {
            System.out.println("Login successful");
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<String> register(@RequestBody  SignUpRequest signUpRequest) {
        CustomerDTO newCustomer= customerService.registerCustomer(signUpRequest.Username, signUpRequest.Password,signUpRequest.EmailAddress);
        if(newCustomer.getEmail()!=null&&newCustomer.getEmail().equals(signUpRequest.EmailAddress) ){
            return ResponseEntity.ok("signUp successful");
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("register failed");
        }

    }

    @GetMapping("/{username}")
    public ResponseEntity<Long> getCustomer(@PathVariable String username) {
        CustomerDTO customerDTO=customerService.getCustomerByUsername(username);
        if(customerDTO!=null){
            return ResponseEntity.ok(customerDTO.getId());
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(-1L);
        }
    }


     public static  class LoginRequest {
         public String Username ;
         public String Password;
    }

     public static class SignUpRequest {
         public String Username;
         public String EmailAddress;
         public String Password;

    }


}
