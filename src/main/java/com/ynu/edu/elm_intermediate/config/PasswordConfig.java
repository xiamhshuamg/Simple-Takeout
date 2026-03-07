package com.ynu.edu.elm_intermediate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
    //创建BCryptPasswordEncoder实例
    //BCrypt特点：
    // 1. 自动加盐（每次加密结果不同）
    // 2. 计算慢（防止暴力破解）
    // 3. 支持验证：passwordEncoder.matches(明文, 密文)
}