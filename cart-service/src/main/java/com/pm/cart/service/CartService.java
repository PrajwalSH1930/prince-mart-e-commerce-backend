package com.pm.cart.service;

import com.pm.cart.entity.Cart;
import com.pm.cart.entity.CartItem;
import com.pm.cart.exception.GlobalExceptionHandler;
import com.pm.cart.exception.ResourceNotFoundException;
import com.pm.cart.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    private final GlobalExceptionHandler globalExceptionHandler;

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository, GlobalExceptionHandler globalExceptionHandler) {
        this.cartRepository = cartRepository;
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Transactional
    public Cart addToCart(Long userId, Long productId, Long variantId, Integer quantity, BigDecimal price) {
        // 1. Get or Create active cart
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setStatus("active");
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        // 2. Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPriceSnapshot(price); // Update snapshot to latest price
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setVariantId(variantId);
            newItem.setQuantity(quantity);
            newItem.setPriceSnapshot(price);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user: " + userId));
    }
    
    @Transactional
    public Cart updateQuantity(Long userId, Long variantId, Integer quantity) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeFromCart(Long userId, Long variantId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));

        cart.getItems().removeIf(item -> item.getVariantId().equals(variantId));

        return cartRepository.save(cart);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));
        
        cart.setStatus("converted"); // This keeps a history of what was in the cart
        cartRepository.save(cart);
    }
}