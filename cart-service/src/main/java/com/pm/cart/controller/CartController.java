package com.pm.cart.controller;

import com.pm.cart.dto.CartRequest;
import com.pm.cart.entity.Cart;
import com.pm.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    @GetMapping("/welcome")
    public String welcome() {
    	return "Welcome!! This is the Cart Service for the Prince Mart by Prince Inc.";
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(
            @RequestHeader("X-User-Id") Long userId, 
            @RequestBody CartRequest request) { // Changed from @RequestParam to @RequestBody
        
        return ResponseEntity.ok(cartService.addToCart(
                userId, 
                request.getProductId(), 
                request.getVariantId(), 
                request.getQuantity(), 
                request.getPrice()
        ));
    }

    @GetMapping("/my-cart")
    public ResponseEntity<Cart> getMyCart(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }
    
    @PutMapping("/update-quantity")
    public ResponseEntity<Cart> updateQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long variantId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(userId, variantId, quantity));
    }

    @DeleteMapping("/remove-item/{variantId}")
    public ResponseEntity<Cart> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long variantId) {
        return ResponseEntity.ok(cartService.removeFromCart(userId, variantId));
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}