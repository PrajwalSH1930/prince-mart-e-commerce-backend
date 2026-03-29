package com.pm.wishlist.dto;


public class WishlistRequest {
    private Long productId;
    private Long variantId; // Optional
	public WishlistRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public WishlistRequest(Long productId, Long variantId) {
		super();
		this.productId = productId;
		this.variantId = variantId;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public Long getVariantId() {
		return variantId;
	}
	public void setVariantId(Long variantId) {
		this.variantId = variantId;
	}
}