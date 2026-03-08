package com.ynu.edu.elm_intermediate.modules.business.vo;

import java.math.BigDecimal;

public class FoodVO {
    private int id;
    private String name;
    private String desc;
    private BigDecimal price;
    private String img;
    private int quantity; // 前端购物车用，后端默认 0
    private Integer status;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}