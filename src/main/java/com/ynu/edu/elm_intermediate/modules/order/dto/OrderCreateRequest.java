package com.ynu.edu.elm_intermediate.modules.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class OrderCreateRequest {

    @Min(value = 1, message = "businessId 必须是正整数")
    private int businessId;

    // 配送费可选
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @NotNull(message = "items 不能为空")
    @Valid
    private List<Item> items;

    public int getBusinessId() { return businessId; }
    public void setBusinessId(int businessId) { this.businessId = businessId; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        @Min(value = 1, message = "foodId 必须是正整数")
        private int foodId;

        @Min(value = 1, message = "quantity 必须 >= 1")
        private int quantity;

        public int getFoodId() { return foodId; }
        public void setFoodId(int foodId) { this.foodId = foodId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    @Min(value = 1, message = "请选择收货地址")
    private int addressId;

    // getter/setter ...
    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }
}