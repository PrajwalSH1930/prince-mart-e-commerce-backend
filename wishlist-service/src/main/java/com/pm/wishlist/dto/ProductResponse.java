package com.pm.wishlist.dto;

import java.util.List;

public class ProductResponse {
    private Long productId;
    private String name;
    private String mainImageUrl;
    private List<ProductVariantResponse> variants; // Add this list

    public ProductResponse() {}

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }

    public List<ProductVariantResponse> getVariants() { return variants; }
    public void setVariants(List<ProductVariantResponse> variants) { this.variants = variants; }
}