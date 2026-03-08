package com.ynu.edu.elm_intermediate.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "账号不能为空")
    private String account; // 统一叫 account，可能是手机号也可能是纯数字ID

    @NotBlank(message = "密码不能为空")
    @Size(min = 3, message = "密码至少 3 位")
    private String password;

    private String role = "customer"; // 默认注册为用户: customer, business, admin

    // Getters & Setters
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}