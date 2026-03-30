package com.pm.auth.dto;

public class NotificationRequest {
    private String recipient;    // Email address
    private String customerName;
    private String type;         // e.g., "WELCOME" or "REGISTRATION"
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getType() {
		return type;
	}
	public NotificationRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public NotificationRequest(String recipient, String customerName, String type) {
		super();
		this.recipient = recipient;
		this.customerName = customerName;
		this.type = type;
	}
	public void setType(String type) {
		this.type = type;
	}
}