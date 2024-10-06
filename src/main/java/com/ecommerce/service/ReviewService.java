package com.ecommerce.service;

import com.ecommerce.exception.ProductException;
import com.ecommerce.modal.Review;
import com.ecommerce.modal.User;
import com.ecommerce.request.ReviewRequest;
import com.ecommerce.dto.ReviewDto;

public interface ReviewService {

	public Review createReview(ReviewRequest req,User user) throws ProductException;
	
	public ReviewDto getAllReview(Long productId);
	
	
}
