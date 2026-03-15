package com.pm.cart.dto;

import java.math.BigDecimal;

public class CartRequest {
    private Long productId;
    private Long variantId;
    private Integer quantity;
    private BigDecimal price;
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
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public CartRequest(Long productId, Long variantId, Integer quantity, BigDecimal price) {
		super();
		this.productId = productId;
		this.variantId = variantId;
		this.quantity = quantity;
		this.price = price;
	}
	public CartRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
}