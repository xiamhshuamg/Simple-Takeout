package com.ynu.edu.elm_intermediate.modules.business.vo;

import java.math.BigDecimal;

public class BusinessVO {
    private int id;
    private String name;
    private Integer isOpen;
    private String desc;
    private BigDecimal minOrder;
    private BigDecimal deliveryFee;

    private String img;
    private BigDecimal rating;
    private int monthlySales;
    private String distance;
    private String eta;
    private BigDecimal lat;
    private BigDecimal lng;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public BigDecimal getMinOrder() { return minOrder; }
    public void setMinOrder(BigDecimal minOrder) { this.minOrder = minOrder; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public int getMonthlySales() { return monthlySales; }
    public void setMonthlySales(int monthlySales) { this.monthlySales = monthlySales; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
    public Integer getIsOpen() { return isOpen; }
    public void setIsOpen(Integer isOpen) { this.isOpen = isOpen; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
}