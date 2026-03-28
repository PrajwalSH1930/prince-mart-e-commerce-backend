package com.pm.review.service;

import com.pm.review.client.OrderClient;
import com.pm.review.client.UserClient;
import com.pm.review.dto.ProductRatingResponse;
import com.pm.review.dto.ReviewRequest;
import com.pm.review.dto.ReviewResponse;
import com.pm.review.dto.UserResponse; // Added missing import
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

    public ReviewService(ReviewRepository reviewRepository, 
                         OrderClient orderClient, 
                         UserClient userClient) {
        this.reviewRepository = reviewRepository;
        this.orderClient = orderClient;
        this.userClient = userClient;
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
        
        // Return a response DTO instead of raw entity for consistency
        return mapToResponse(savedReview);
    }

    public List<ReviewResponse> getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Helper method to keep code clean and handle name fetching
    private ReviewResponse mapToResponse(Review review) {
        String name = "Verified Customer";
        try {
            UserResponse user = userClient.getUserById(review.getUserId());
            if (user != null && user.getFullName() != null) {
                name = user.getFullName();
            }
        } catch (Exception e) {
            // Fallback to "Verified Customer" if Identity service is down
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
}