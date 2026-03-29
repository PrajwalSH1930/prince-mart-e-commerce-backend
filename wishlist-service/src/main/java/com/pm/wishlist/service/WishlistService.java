package com.pm.wishlist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.wishlist.client.AuditClient; // New client
import com.pm.wishlist.client.ProductClient;
import com.pm.wishlist.dto.*; // New DTO
import com.pm.wishlist.entity.Wishlist;
import com.pm.wishlist.entity.WishlistItem;
import com.pm.wishlist.repository.WishlistItemRepository;
import com.pm.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository itemRepository;
    private final ProductClient productClient;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public WishlistService(WishlistRepository wishlistRepository, 
                           WishlistItemRepository itemRepository,
                           ProductClient productClient,
                           AuditClient auditClient,
                           ObjectMapper objectMapper) {
        this.wishlistRepository = wishlistRepository;
        this.itemRepository = itemRepository;
        this.productClient = productClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    public Wishlist getMyWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUserId(userId);
                    return wishlistRepository.save(newWishlist);
                });
    }

    @Transactional
    public Wishlist addItem(Long userId, WishlistRequest request) {
        Wishlist wishlist = getMyWishlist(userId);

        boolean exists = itemRepository.existsByWishlistUserIdAndProductIdAndVariantId(
                userId, request.getProductId(), request.getVariantId());

        if (!exists) {
            WishlistItem item = new WishlistItem();
            item.setWishlist(wishlist);
            item.setProductId(request.getProductId());
            item.setVariantId(request.getVariantId());
            WishlistItem savedItem = itemRepository.save(item);
            wishlist.getItems().add(savedItem);

            // Audit the addition
            sendAuditLog(userId, "WISHLIST_ADD_ITEM", null, savedItem);
        }
        return wishlist;
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        // Capture data before deletion for audit
        String dataBefore = "ProductID: " + productId;
        
        itemRepository.deleteByWishlistUserIdAndProductId(userId, productId);
        
        // Audit the removal
        sendAuditLog(userId, "WISHLIST_REMOVE_ITEM", dataBefore, "REMOVED");
    }

    @Transactional
    public void clearWishlist(Long userId) {
        wishlistRepository.findByUserId(userId).ifPresent(wishlist -> {
            String dataBefore = "Items Count: " + wishlist.getItems().size();
            
            wishlist.getItems().clear();
            wishlistRepository.save(wishlist);
            
            // Audit the clear action
            sendAuditLog(userId, "WISHLIST_CLEAR_ALL", dataBefore, "EMPTY");
        });
    }
    
    public WishlistResponse getMyWishlistDetails(Long userId) {
        Wishlist wishlist = getMyWishlist(userId);
        
        List<WishlistResponse.WishlistItemDto> itemDtos = wishlist.getItems().stream().map(item -> {
            String name = "Product Unavailable";
            BigDecimal price = BigDecimal.ZERO;
            String image = "";

            try {
                ProductResponse product = productClient.getProductById(item.getProductId());
                if (product != null) {
                    name = product.getName();
                    image = product.getMainImageUrl();
                    
                    if (item.getVariantId() == null && product.getVariants() != null && !product.getVariants().isEmpty()) {
                        price = product.getVariants().get(0).getPrice();
                    }
                }

                if (item.getVariantId() != null) {
                    ProductVariantResponse variant = productClient.getVariantById(item.getVariantId());
                    if (variant != null) {
                        price = variant.getPrice();
                    }
                }
            } catch (Exception e) {
                System.err.println("Feign Call Failed for Item: " + item.getProductId() + " Error: " + e.getMessage());
            }

            return WishlistResponse.WishlistItemDto.builder()
                    .wishlistItemId(item.getWishlistItemId())
                    .productId(item.getProductId())
                    .productName(name)
                    .price(price)
                    .imageUrl(image)
                    .build();
        }).toList();

        return WishlistResponse.builder()
                .wishlistId(wishlist.getWishlistId())
                .userId(wishlist.getUserId())
                .items(itemDtos)
                .build();
    }

    // Centralized Helper for External Auditing
    private void sendAuditLog(Long userId, String action, Object dataBefore, Object dataAfter) {
        try {
            String before = dataBefore != null ? (dataBefore instanceof String ? (String) dataBefore : objectMapper.writeValueAsString(dataBefore)) : null;
            String after = dataAfter != null ? (dataAfter instanceof String ? (String) dataAfter : objectMapper.writeValueAsString(dataAfter)) : null;

            auditClient.createLog(new AuditLogRequest(
                "WISHLIST-SERVICE", 
                action, 
                userId, 
                before, 
                after
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed in Wishlist Service: " + e.getMessage());
        }
    }
}