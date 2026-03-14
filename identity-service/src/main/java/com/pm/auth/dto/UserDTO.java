package com.pm.auth.dto;

public class UserDTO {
    private String email;
    private String password; // Raw password from user
    private String phone;
    private String role;

    public UserDTO() {}

    public UserDTO(String email, String password, String phone, String role) {
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}