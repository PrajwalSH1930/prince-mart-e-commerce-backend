package com.pm.review.dto;

public class UserResponse {
    private Long userId;
    private String fullName;
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
    public UserResponse(Long userId, String fullName) {
		this.userId = userId;
		this.fullName = fullName;
	}
    public UserResponse() {
		// Default constructor
	}
    
}