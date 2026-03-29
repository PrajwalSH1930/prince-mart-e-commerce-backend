package com.pm.wishlist.controller;

import com.pm.wishlist.dto.WishlistRequest;
import com.pm.wishlist.dto.WishlistResponse;
import com.pm.wishlist.entity.Wishlist;
import com.pm.wishlist.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> getMyWishlist(@RequestHeader("X-User-Id") Long userId) {
        // Changed from getMyWishlist to getMyWishlistDetails
        return ResponseEntity.ok(wishlistService.getMyWishlistDetails(userId));
    }
    
    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
		return ResponseEntity.ok("Welcome!! This is the Wishlist Service for Prince Mart by Prince Inc.");
	}

    @PostMapping("/add")
    public ResponseEntity<Wishlist> addToWishlist(
            @RequestHeader("X-User-Id") Long userId, 
            @RequestBody WishlistRequest request) {
        return ResponseEntity.ok(wishlistService.addItem(userId, request));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @RequestHeader("X-User-Id") Long userId, 
            @PathVariable Long productId) {
        wishlistService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }  
}