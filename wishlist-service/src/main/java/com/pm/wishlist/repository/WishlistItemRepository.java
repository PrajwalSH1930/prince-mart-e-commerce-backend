package com.pm.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pm.wishlist.entity.WishlistItem;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
	boolean existsByWishlistUserIdAndProductIdAndVariantId(Long userId, Long productId, Long variantId);
    
    // To remove a specific item
    void deleteByWishlistUserIdAndProductId(Long userId, Long productId);
}
