package com.pm.review.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.review.client.AuditClient; // New client
import com.pm.review.client.OrderClient;
import com.pm.review.client.UserClient;
import com.pm.review.dto.*;
import com.pm.review.entity.Review;
import com.pm.review.exception.ResourceNotFoundException;
import com.pm.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderClient orderClient;
    private final UserClient userClient;
    private final AuditClient auditClient; // New
    private final ObjectMapper objectMapper; // New

    public ReviewService(ReviewRepository reviewRepository, 
                         OrderClient orderClient, 
                         UserClient userClient,
                         AuditClient auditClient,
                         ObjectMapper objectMapper) {
        this.reviewRepository = reviewRepository;
        this.orderClient = orderClient;
        this.userClient = userClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ReviewResponse postReview(Long userId, ReviewRequest request) {
        // 1. Rule Check: Did they buy it?
        boolean purchased = orderClient.hasPurchasedProduct(userId, request.getProductId());
        if (!purchased) {
            throw new ResourceNotFoundException("Sorry, you can only review products you have actually purchased from Prince Mart.");
        }

        // 2. Rule Check: Already reviewed?
        if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new ResourceNotFoundException("You have already reviewed this product.");
        }

        // 3. Map and Save
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(request.getProductId());
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);
        
        // Audit the review posting
        sendAuditLog(userId, "POST_REVIEW", null, savedReview);
        
        // Return a response DTO
        return mapToResponse(savedReview);
    }

    public List<ReviewResponse> getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ReviewResponse mapToResponse(Review review) {
        String name = "Verified Customer";
        try {
            UserResponse user = userClient.getUserById(review.getUserId());
            if (user != null && user.getFullName() != null) {
                name = user.getFullName();
            }
        } catch (Exception e) {
            System.err.println("Could not fetch user name for ID " + review.getUserId() + ": " + e.getMessage());
        }

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .customerName(name)
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
    
    public ProductRatingResponse getProductRatingSummary(Long productId) {
        Double avg = reviewRepository.getAverageRating(productId);
        Long count = reviewRepository.getReviewCount(productId);
        
        return new ProductRatingResponse(productId, avg, count);
    }
    
    public List<ReviewResponse> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();

        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Centralized Helper for External Auditing
    private void sendAuditLog(Long userId, String action, Object dataBefore, Object dataAfter) {
        try {
            String before = dataBefore != null ? (dataBefore instanceof String ? (String) dataBefore : objectMapper.writeValueAsString(dataBefore)) : null;
            String after = dataAfter != null ? (dataAfter instanceof String ? (String) dataAfter : objectMapper.writeValueAsString(dataAfter)) : null;

            auditClient.createLog(new AuditLogRequest(
                "REVIEW-SERVICE", 
                action, 
                userId, 
                before, 
                after
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed in Review Service: " + e.getMessage());
        }
    }
}