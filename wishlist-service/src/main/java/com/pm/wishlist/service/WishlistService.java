package com.pm.wishlist.service;

import com.pm.wishlist.client.ProductClient;
import com.pm.wishlist.dto.ProductResponse;
import com.pm.wishlist.dto.ProductVariantResponse;
import com.pm.wishlist.dto.WishlistRequest;
import com.pm.wishlist.dto.WishlistResponse;
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

    public WishlistService(WishlistRepository wishlistRepository, 
                           WishlistItemRepository itemRepository,
                           ProductClient productClient) {
        this.wishlistRepository = wishlistRepository;
        this.itemRepository = itemRepository;
        this.productClient = productClient;
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
            itemRepository.save(item);
            wishlist.getItems().add(item);
        }
        return wishlist;
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        itemRepository.deleteByWishlistUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearWishlist(Long userId) {
        wishlistRepository.findByUserId(userId).ifPresent(wishlist -> {
            wishlist.getItems().clear();
            wishlistRepository.save(wishlist);
        });
    }
    
    public WishlistResponse getMyWishlistDetails(Long userId) {
        Wishlist wishlist = getMyWishlist(userId);
        
        List<WishlistResponse.WishlistItemDto> itemDtos = wishlist.getItems().stream().map(item -> {
            String name = "Product Unavailable";
            BigDecimal price = BigDecimal.ZERO;
            String image = "";

            try {
                // 1. Fetch Base Product Info
                ProductResponse product = productClient.getProductById(item.getProductId());
                if (product != null) {
                    name = product.getName();
                    image = product.getMainImageUrl();
                    
                    // Fallback: If no specific variant was wishlisted, use first variant price
                    if (item.getVariantId() == null && product.getVariants() != null && !product.getVariants().isEmpty()) {
                        price = product.getVariants().get(0).getPrice();
                    }
                }

                // 2. Fetch Specific Variant Price if variantId exists
                if (item.getVariantId() != null) {
                    ProductVariantResponse variant = productClient.getVariantById(item.getVariantId());
                    if (variant != null) {
                        price = variant.getPrice();
                    }
                }
            } catch (Exception e) {
                System.err.println("Feign Call Failed for Item: " + item.getProductId() + " Error: " + e.getMessage());
            }

            // Using the Manual Builder we created in the WishlistResponse DTO
            return WishlistResponse.WishlistItemDto.builder()
                    .wishlistItemId(item.getWishlistItemId())
                    .productId(item.getProductId())
                    .productName(name)
                    .price(price)
                    .imageUrl(image)
                    .build();
        }).toList();

        // Using the Manual Builder for the main response
        return WishlistResponse.builder()
                .wishlistId(wishlist.getWishlistId())
                .userId(wishlist.getUserId())
                .items(itemDtos)
                .build();
    }
}