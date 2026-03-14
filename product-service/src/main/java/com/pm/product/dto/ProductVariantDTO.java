package com.pm.product.dto;

import java.math.BigDecimal;

public class ProductVariantDTO {
    private String size;
    private String color;
    private BigDecimal price;
    private String currency;
    private Integer stockQuantity;
    private String sku;

    public ProductVariantDTO() {}

    // Getters and Setters
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
}