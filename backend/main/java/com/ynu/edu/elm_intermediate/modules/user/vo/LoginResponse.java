package com.ynu.edu.elm_intermediate.modules.user.vo;

public class LoginResponse {
    private int id;       // 用户ID / 商家ID / 管理员ID
    private String token;
    private String role;  // 身份: "customer", "business", "admin"
    private String name;  // 显示名称

    public LoginResponse(int id, String token, String role, String name) {
        this.id = id;
        this.token = token;
        this.role = role;
        this.name = name;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}