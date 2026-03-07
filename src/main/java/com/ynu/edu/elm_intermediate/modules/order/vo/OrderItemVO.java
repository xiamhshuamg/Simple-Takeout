package com.ynu.edu.elm_intermediate.modules.order.vo;

import java.math.BigDecimal;

public class OrderItemVO {
    private String name;
    private BigDecimal price;
    private int quantity;

    public OrderItemVO(String name, BigDecimal price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
}