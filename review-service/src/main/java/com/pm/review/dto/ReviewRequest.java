package com.pm.review.dto;

public class ReviewRequest {
    
    private Long productId;
    
    // The rating (e.g., 1, 2, 3, 4, 5)
    private int rating;
    
    private String title;
    
    private String comment;

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ReviewRequest(Long productId, int rating, String title, String comment) {
		super();
		this.productId = productId;
		this.rating = rating;
		this.title = title;
		this.comment = comment;
	}

	public ReviewRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
}