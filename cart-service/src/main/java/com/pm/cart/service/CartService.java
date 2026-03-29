package com.pm.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper; // For JSON conversion
import com.pm.cart.client.AuditClient; // Your new client
import com.pm.cart.dto.AuditLogRequest; // Your new DTO
import com.pm.cart.entity.Cart;
import com.pm.cart.entity.CartItem;
import com.pm.cart.exception.ResourceNotFoundException;
import com.pm.cart.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public CartService(CartRepository cartRepository, 
                       AuditClient auditClient, 
                       ObjectMapper objectMapper) {
        this.cartRepository = cartRepository;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
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

        // Capture state before change
        String dataBefore = null;
        try {
            dataBefore = objectMapper.writeValueAsString(cart);
        } catch (Exception e) {
            System.err.println("JSON conversion failed: " + e.getMessage());
        }

        // 2. Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPriceSnapshot(price); 
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setVariantId(variantId);
            newItem.setQuantity(quantity);
            newItem.setPriceSnapshot(price);
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);

        // Audit the change
        sendAuditLog(userId, "ADD_TO_CART", dataBefore, savedCart);

        return savedCart;
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user: " + userId));
    }
    
    @Transactional
    public Cart updateQuantity(Long userId, Long variantId, Integer quantity) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));

        String dataBefore = null;
        try {
            dataBefore = objectMapper.writeValueAsString(cart);
        } catch (Exception e) {}

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        Cart savedCart = cartRepository.save(cart);
        sendAuditLog(userId, "UPDATE_CART_QUANTITY", dataBefore, savedCart);
        
        return savedCart;
    }

    @Transactional
    public Cart removeFromCart(Long userId, Long variantId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));

        String dataBefore = null;
        try {
            dataBefore = objectMapper.writeValueAsString(cart);
        } catch (Exception e) {}

        cart.getItems().removeIf(item -> item.getVariantId().equals(variantId));
        Cart savedCart = cartRepository.save(cart);

        sendAuditLog(userId, "REMOVE_FROM_CART", dataBefore, savedCart);

        return savedCart;
    }
    
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for user: " + userId));
        
        String dataBefore = null;
        try {
            dataBefore = objectMapper.writeValueAsString(cart);
        } catch (Exception e) {}
        
        cart.getItems().clear(); 
        cart.setStatus("COMPLETED"); 
        
        Cart savedCart = cartRepository.save(cart);
        sendAuditLog(userId, "CLEAR_CART_COMPLETED", dataBefore, savedCart);
    }

    // Helper method to keep main code clean
    private void sendAuditLog(Long userId, String action, String dataBefore, Cart savedCart) {
        try {
            String dataAfter = objectMapper.writeValueAsString(savedCart);
            auditClient.createLog(new AuditLogRequest(
                "CART-SERVICE", 
                action, 
                userId, 
                dataBefore, 
                dataAfter
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed: " + e.getMessage());
        }
    }
}