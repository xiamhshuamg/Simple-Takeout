package com.ynu.edu.elm_intermediate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("customer")
public class Customer {
    @TableId(type = IdType.AUTO)
    private Integer customerId;
    private Long customerAccount;
    private String password;
    private String userName;
    private String avatar;
    private BigDecimal balance;

    // === 手动添加 Getter 和 Setter ===
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Long getCustomerAccount() { return customerAccount; }
    public void setCustomerAccount(Long customerAccount) { this.customerAccount = customerAccount; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}