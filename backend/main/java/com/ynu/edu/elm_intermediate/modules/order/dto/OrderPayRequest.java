package com.ynu.edu.elm_intermediate.modules.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class OrderPayRequest {

    @Min(value = 1, message = "orderId 必须是正整数")
    private int orderId;

    @NotBlank(message = "payMethod 不能为空")
    private String payMethod;

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
}