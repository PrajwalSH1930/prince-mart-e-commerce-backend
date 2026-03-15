package com.pm.order.dto;

public class OrderRequest {
    private Long shippingAddressId;
    private Long billingAddressId;
    private String currency; // e.g., "INR"
	public Long getShippingAddressId() {
		return shippingAddressId;
	}
	public void setShippingAddressId(Long shippingAddressId) {
		this.shippingAddressId = shippingAddressId;
	}
	public Long getBillingAddressId() {
		return billingAddressId;
	}
	public void setBillingAddressId(Long billingAddressId) {
		this.billingAddressId = billingAddressId;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public OrderRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public OrderRequest(Long shippingAddressId, Long billingAddressId, String currency) {
		super();
		this.shippingAddressId = shippingAddressId;
		this.billingAddressId = billingAddressId;
		this.currency = currency;
	}
}