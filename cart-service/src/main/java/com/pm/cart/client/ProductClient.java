package com.pm.cart.client;

import com.pm.cart.dto.ProductVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {

    /**
     * Hits the Product Service to get official variant details (including price).
     * The path should match your ProductController's @GetMapping for variants.
     */
    @GetMapping("/products/variants/{variantId}")
    ProductVariantResponse getVariantById(@PathVariable("variantId") Long variantId);
}