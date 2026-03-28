package com.pm.payment.dto;

public class NotificationRequest {
    private String recipient;
    private String subject;
    private String customerName;
    private String orderId;
    private String amount;

    // Standard Getters and Setters (No Lombok)
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
}