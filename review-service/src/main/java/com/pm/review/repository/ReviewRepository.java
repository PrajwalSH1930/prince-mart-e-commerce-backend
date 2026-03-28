package com.pm.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pm.review.entity.Review;

import feign.Param;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    // To show a User all the reviews they have written
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Check if a user has already reviewed a product (to prevent spam)
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    Long getReviewCount(@Param("productId") Long productId);
}
