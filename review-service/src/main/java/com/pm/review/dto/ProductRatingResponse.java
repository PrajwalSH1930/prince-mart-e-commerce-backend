package com.pm.review.dto;

public class ProductRatingResponse {
    private Long productId;
    private Double averageRating;
    private Long totalReviews;

    public ProductRatingResponse(Long productId, Double averageRating, Long totalReviews) {
        this.productId = productId;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
    }

    // Standard Getters
    public Long getProductId() { return productId; }
    public Double getAverageRating() { return averageRating; }
    public Long getTotalReviews() { return totalReviews; }
}