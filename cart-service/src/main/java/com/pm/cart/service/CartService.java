package com.pm.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.cart.client.AuditClient;
import com.pm.cart.client.ProductClient; // NEW: Added ProductClient
import com.pm.cart.dto.AuditLogRequest;
import com.pm.cart.dto.ProductVariantResponse; // NEW: Added DTO for variant info
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
    private final ProductClient productClient; // NEW
    private final ObjectMapper objectMapper;

    public CartService(CartRepository cartRepository, 
                       AuditClient auditClient, 
                       ProductClient productClient, // NEW
                       ObjectMapper objectMapper) {
        this.cartRepository = cartRepository;
        this.auditClient = auditClient;
        this.productClient = productClient; // NEW
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Cart addToCart(Long userId, Long productId, Long variantId, Integer quantity) {
        // 1. SECURITY FIX: Fetch the REAL price from Product Service
        BigDecimal realPrice;
        try {
            ProductVariantResponse variant = productClient.getVariantById(variantId);
            realPrice = variant.getPrice();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Price verification failed for variant " + variantId + ". Service may be down.");
        }

        // 2. Get or Create active cart
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

        // 3. Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPriceSnapshot(realPrice); // Use official price
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setVariantId(variantId);
            newItem.setQuantity(quantity);
            newItem.setPriceSnapshot(realPrice); // Use official price
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);

        // Audit the change
        sendAuditLog(userId, "ADD_TO_CART_SECURE", dataBefore, savedCart);

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
            
            // OPTIONAL: Refresh price snapshot on quantity update to ensure accuracy
            try {
                ProductVariantResponse variant = productClient.getVariantById(variantId);
                item.setPriceSnapshot(variant.getPrice());
            } catch (Exception e) {
                System.err.println("Price refresh failed during quantity update: " + e.getMessage());
            }
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