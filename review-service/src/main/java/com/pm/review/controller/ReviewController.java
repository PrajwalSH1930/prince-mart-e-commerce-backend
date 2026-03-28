package com.pm.review.controller;

import com.pm.review.dto.ProductRatingResponse;
import com.pm.review.dto.ReviewRequest;
import com.pm.review.dto.ReviewResponse;
import com.pm.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/post")
    public ResponseEntity<ReviewResponse> postReview(
            @RequestHeader("X-User-Id") Long userId, 
            @RequestBody ReviewRequest request) {
        
        // service now returns ReviewResponse
        return ResponseEntity.ok(reviewService.postReview(userId, request));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }
    
 // Inside ReviewController.java

    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ProductRatingResponse> getProductRatingSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductRatingSummary(productId));
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
		return ResponseEntity.ok(reviewService.getAllReviews());
	}
}