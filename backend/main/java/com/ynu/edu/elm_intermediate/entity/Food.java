package com.ynu.edu.elm_intermediate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("food")
public class Food {
    @TableId(type = IdType.AUTO)
    private Integer foodId;
    private Integer businessId;
    private String foodName;
    private String foodExplain;
    private String foodImg;
    private BigDecimal foodPrice;
    private Integer status;

    // === 手动添加 Getter 和 Setter ===
    public Integer getFoodId() { return foodId; }
    public void setFoodId(Integer foodId) { this.foodId = foodId; }

    public Integer getBusinessId() { return businessId; }
    public void setBusinessId(Integer businessId) { this.businessId = businessId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodExplain() { return foodExplain; }
    public void setFoodExplain(String foodExplain) { this.foodExplain = foodExplain; }

    public String getFoodImg() { return foodImg; }
    public void setFoodImg(String foodImg) { this.foodImg = foodImg; }

    public BigDecimal getFoodPrice() { return foodPrice; }
    public void setFoodPrice(BigDecimal foodPrice) { this.foodPrice = foodPrice; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}