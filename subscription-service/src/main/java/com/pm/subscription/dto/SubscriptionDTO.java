package com.pm.subscription.dto;

public class SubscriptionDTO {
	String email;
	
	public SubscriptionDTO() {
		super();
	}
	
	public SubscriptionDTO(String email) {
		super();
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
}
