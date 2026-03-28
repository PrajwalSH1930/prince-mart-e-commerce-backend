package com.pm.review.dto;

import java.time.LocalDateTime;

public class ReviewResponse {
    private Long reviewId;
    private Long productId;
    private Long userId;
    private String customerName;
    private int rating;
    private String title;
    private String comment;
    private LocalDateTime createdAt;

    // Standard Constructor
    public ReviewResponse() {}

    // Full Constructor for Builder
    public ReviewResponse(Long reviewId, Long productId, Long userId, String customerName, 
                          int rating, String title, String comment, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.userId = userId;
        this.customerName = customerName;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getReviewId() { return reviewId; }
    public Long getProductId() { return productId; }
    public Long getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public int getRating() { return rating; }
    public String getTitle() { return title; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Static Builder Trigger
    public static ReviewResponseBuilder builder() {
        return new ReviewResponseBuilder();
    }

    // Inner Builder Class
    public static class ReviewResponseBuilder {
        private Long reviewId;
        private Long productId;
        private Long userId;
        private String customerName;
        private int rating;
        private String title;
        private String comment;
        private LocalDateTime createdAt;

        public ReviewResponseBuilder reviewId(Long reviewId) { this.reviewId = reviewId; return this; }
        public ReviewResponseBuilder productId(Long productId) { this.productId = productId; return this; }
        public ReviewResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public ReviewResponseBuilder customerName(String customerName) { this.customerName = customerName; return this; }
        public ReviewResponseBuilder rating(int rating) { this.rating = rating; return this; }
        public ReviewResponseBuilder title(String title) { this.title = title; return this; }
        public ReviewResponseBuilder comment(String comment) { this.comment = comment; return this; }
        public ReviewResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ReviewResponse build() {
            return new ReviewResponse(reviewId, productId, userId, customerName, rating, title, comment, createdAt);
        }
    }
}