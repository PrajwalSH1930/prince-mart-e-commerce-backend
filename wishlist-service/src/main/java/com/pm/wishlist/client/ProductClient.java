package com.pm.wishlist.client;

import com.pm.wishlist.dto.ProductResponse;
import com.pm.wishlist.dto.ProductVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {

    @GetMapping("/products/{productId}")
    ProductResponse getProductById(@PathVariable("productId") Long productId);

    @GetMapping("/products/variants/{variantId}")
    ProductVariantResponse getVariantById(@PathVariable("variantId") Long variantId);
}