package com.pm.wishlist.dto;

import java.math.BigDecimal;

public class ProductVariantResponse {
    private Long variantId;
    private BigDecimal price;
    private String currency;

    public ProductVariantResponse() {}

    // Getters and Setters
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}