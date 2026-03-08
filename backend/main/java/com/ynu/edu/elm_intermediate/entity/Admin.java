package com.ynu.edu.elm_intermediate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 管理员表实体
 */
@TableName("admin")
public class Admin {

    @TableId(value = "adminId", type = IdType.AUTO)
    private Integer adminId;
    @TableField("adminAccount")
    private String adminAccount;
    private String password;
    @TableField("adminName")
    private String adminName;

    /** 管理员头像 */
    private String avatar;

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public String getAdminAccount() { return adminAccount; }
    public void setAdminAccount(String adminAccount) { this.adminAccount = adminAccount; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
