package com.pm.wishlist.dto;

import java.math.BigDecimal;
import java.util.List;

public class WishlistResponse {
    private Long wishlistId;
    private Long userId;
    private List<WishlistItemDto> items;

    public WishlistResponse(Long wishlistId, Long userId, List<WishlistItemDto> items) {
        this.wishlistId = wishlistId;
        this.userId = userId;
        this.items = items;
    }

    public Long getWishlistId() { return wishlistId; }
    public Long getUserId() { return userId; }
    public List<WishlistItemDto> getItems() { return items; }

    public static WishlistResponseBuilder builder() {
        return new WishlistResponseBuilder();
    }

    public static class WishlistResponseBuilder {
        private Long wishlistId;
        private Long userId;
        private List<WishlistItemDto> items;

        public WishlistResponseBuilder wishlistId(Long wishlistId) { this.wishlistId = wishlistId; return this; }
        public WishlistResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public WishlistResponseBuilder items(List<WishlistItemDto> items) { this.items = items; return this; }

        public WishlistResponse build() {
            return new WishlistResponse(wishlistId, userId, items);
        }
    }

    // Static Inner DTO for Items
    public static class WishlistItemDto {
        private Long wishlistItemId;
        private Long productId;
        private String productName;
        private String imageUrl;
        private BigDecimal price;

        public WishlistItemDto(Long wishlistItemId, Long productId, String productName, String imageUrl, BigDecimal price) {
            this.wishlistItemId = wishlistItemId;
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.price = price;
        }

        // Getters
        public Long getWishlistItemId() { return wishlistItemId; }
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getImageUrl() { return imageUrl; }
        public BigDecimal getPrice() { return price; }

        public static WishlistItemDtoBuilder builder() {
            return new WishlistItemDtoBuilder();
        }

        public static class WishlistItemDtoBuilder {
            private Long wishlistItemId;
            private Long productId;
            private String productName;
            private String imageUrl;
            private BigDecimal price;

            public WishlistItemDtoBuilder wishlistItemId(Long wishlistItemId) { this.wishlistItemId = wishlistItemId; return this; }
            public WishlistItemDtoBuilder productId(Long productId) { this.productId = productId; return this; }
            public WishlistItemDtoBuilder productName(String productName) { this.productName = productName; return this; }
            public WishlistItemDtoBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
            public WishlistItemDtoBuilder price(BigDecimal price) { this.price = price; return this; }

            public WishlistItemDto build() {
                return new WishlistItemDto(wishlistItemId, productId, productName, imageUrl, price);
            }
        }
    }
}