package com.pm.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/identity-fallback")
    public Mono<String> identityServiceFallback() {
        return Mono.just("Login/Registration is temporarily unavailable. Please try again in a moment.");
    }

    @GetMapping("/product-fallback")
    public Mono<String> productServiceFallback() {
        return Mono.just("Product Catalog is taking too long to respond. Prince Mart is working on it!");
    }

    @GetMapping("/inventory-fallback")
    public Mono<String> inventoryServiceFallback() {
        return Mono.just("Inventory check failed. We're verifying stock levels, please hold on.");
    }

    @GetMapping("/cart-fallback")
    public Mono<String> cartServiceFallback() {
        return Mono.just("Your cart is safe, but we're having trouble reaching it. Try refreshing!");
    }

    @GetMapping("/order-fallback")
    public Mono<String> orderServiceFallback() {
        return Mono.just("Order processing is slow. Please check your order history in a few minutes.");
    }

    @GetMapping("/payment-fallback")
    public Mono<String> paymentServiceFallback() {
        return Mono.just("Payment Gateway is busy. If money was deducted, don't worry, we'll sync it soon.");
    }

    @GetMapping("/notification-fallback")
    public Mono<String> notificationServiceFallback() {
        return Mono.just("Notification Service is delayed. You will receive your updates shortly.");
    }

    @GetMapping("/shipping-fallback")
    public Mono<String> shippingServiceFallback() {
        return Mono.just("Tracking information is currently unavailable. Your package is still moving!");
    }

    @GetMapping("/review-fallback")
    public Mono<String> reviewServiceFallback() {
        return Mono.just("Reviews are temporarily offline. Your feedback is still important to us!");
    }

    @GetMapping("/wishlist-fallback")
    public Mono<String> wishlistServiceFallback() {
        return Mono.just("Wishlist Service is resting. Your favorite items will be back soon.");
    }

    @GetMapping("/coupon-fallback")
    public Mono<String> couponServiceFallback() {
        return Mono.just("Coupon validation is down. We'll try to apply your discount at checkout.");
    }

    @GetMapping("/audit-fallback")
    public Mono<String> auditServiceFallback() {
        return Mono.just("Audit logging is delayed. System security is still active.");
    }
}