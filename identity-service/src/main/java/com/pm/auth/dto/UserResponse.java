package com.pm.auth.dto; // Change to com.pm.order.dto in the Order Service

public class UserResponse {
    private Long id;
    private String email;
    private String fullName; // This will now hold (firstName + lastName)
    private String phone;

    // Constructors
    public UserResponse() {}

    public UserResponse(Long id, String email, String fullName, String phone) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}