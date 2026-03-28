package com.pm.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ORDER-SERVICE")
public interface OrderClient {

    // New endpoint we'll need in Order Service to check purchase history
    @GetMapping("/orders/check-purchase/{userId}/{productId}")
    boolean hasPurchasedProduct(
        @PathVariable("userId") Long userId, 
        @PathVariable("productId") Long productId
    );
}