package com.ynu.edu.elm_intermediate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("delivery_address")
public class DeliveryAddress {
    @TableId(value = "daId", type = IdType.AUTO)
    private Integer daId;
    private Integer customerId;
    private String contactName;
    private Integer contactSex;
    private String contactTel;
    private String address;
    private Integer isDefault;

    // === 手动添加 Getter 和 Setter ===
    public Integer getDaId() { return daId; }
    public void setDaId(Integer daId) { this.daId = daId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public Integer getContactSex() { return contactSex; }
    public void setContactSex(Integer contactSex) { this.contactSex = contactSex; }

    public String getContactTel() { return contactTel; }
    public void setContactTel(String contactTel) { this.contactTel = contactTel; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
}