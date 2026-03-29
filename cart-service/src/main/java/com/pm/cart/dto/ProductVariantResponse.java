package com.pm.cart.dto;

import java.math.BigDecimal;

public class ProductVariantResponse {
    private Long variantId;
    private String size;
    private String color;
    private BigDecimal price;
    private String sku;
    private Integer stockQuantity;

    public ProductVariantResponse() {}

    public ProductVariantResponse(Long variantId, String size, String color, BigDecimal price, String sku, Integer stockQuantity) {
        this.variantId = variantId;
        this.size = size;
        this.color = color;
        this.price = price;
        this.sku = sku;
        this.stockQuantity = stockQuantity;
    }

    // Getters and Setters
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}