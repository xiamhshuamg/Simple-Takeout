package com.ynu.edu.elm_intermediate;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("1234")); // 这里改成你想要的管理员密码
    }
}
