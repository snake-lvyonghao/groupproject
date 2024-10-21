package com.comp5348.store.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    private int version;

    @Column(nullable = false)
    private String name;


    @Column(nullable = false)
    private String password;  // 密码字段

    @Email  // 验证是否符合邮箱格式
    @Column(nullable = false, unique = true)  // 设置为唯一，防止多个用户使用同一邮箱注册
    private String email;  // 修改字段名为 email, 避免与注解名冲突

    // 使用 BCryptPasswordEncoder 对密码进行加密
    public void encryptAndSetPassword(String rawPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        this.password = passwordEncoder.encode(rawPassword);  // 加密密码
    }

    // 验证密码
    public boolean checkPassword(String rawPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, this.password);  // 验证密码
    }

}
