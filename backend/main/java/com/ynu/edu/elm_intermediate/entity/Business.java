package com.ynu.edu.elm_intermediate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("business")
public class Business {
    @TableId(type = IdType.AUTO)
    private Integer businessId;
    private Long businessAccount;
    private String password;
    private String businessName;
    private String businessExplain;
    private Integer isOpen;
    private String businessImg;
    private BigDecimal starPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal rating;
    private Integer monthlySales;
    private String distance;
    private String eta;
    private BigDecimal lat;
    private BigDecimal lng;

    // === 手动添加 Getter 和 Setter ===
    public Integer getBusinessId() { return businessId; }
    public void setBusinessId(Integer businessId) { this.businessId = businessId; }

    public Long getBusinessAccount() { return businessAccount; }
    public void setBusinessAccount(Long businessAccount) { this.businessAccount = businessAccount; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessExplain() { return businessExplain; }
    public void setBusinessExplain(String businessExplain) { this.businessExplain = businessExplain; }

    public String getBusinessImg() { return businessImg; }
    public void setBusinessImg(String businessImg) { this.businessImg = businessImg; }

    public BigDecimal getStarPrice() { return starPrice; }
    public void setStarPrice(BigDecimal starPrice) { this.starPrice = starPrice; }

    public BigDecimal getDeliveryPrice() { return deliveryPrice; }
    public void setDeliveryPrice(BigDecimal deliveryPrice) { this.deliveryPrice = deliveryPrice; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getMonthlySales() { return monthlySales; }
    public void setMonthlySales(Integer monthlySales) { this.monthlySales = monthlySales; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }

    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }

    public Integer getIsOpen() { return isOpen; }
    public void setIsOpen(Integer isOpen) { this.isOpen = isOpen; }
}