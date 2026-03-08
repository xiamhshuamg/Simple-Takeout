package com.ynu.edu.elm_intermediate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("order_item")
public class OrderItem {
    @TableId(value = "orderItemId", type = IdType.AUTO)
    private Integer orderItemId;
    private Integer orderId;
    private Integer foodId;
    private String foodName;
    private BigDecimal foodPrice;
    private Integer quantity;

    // === 手动添加 Getter 和 Setter ===
    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getFoodId() { return foodId; }
    public void setFoodId(Integer foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public BigDecimal getFoodPrice() { return foodPrice; }
    public void setFoodPrice(BigDecimal foodPrice) { this.foodPrice = foodPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}