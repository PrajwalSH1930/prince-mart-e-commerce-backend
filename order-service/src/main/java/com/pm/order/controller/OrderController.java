package com.pm.order.controller;

import com.pm.order.dto.OrderRequest;
import com.pm.order.entity.Order;
import com.pm.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome!! This is the Order Service for Prince Mart by Prince Inc.";
    }

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(
            @RequestHeader("X-User-Id") Long userId, 
            @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(userId, request));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<Order>> getOrderHistory(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getOrderHistory(userId));
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Order> getOrderDetails(
            @PathVariable Long orderId, 
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getOrderDetails(orderId, userId));
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long orderId,
            @RequestParam String paymentStatus,
            @RequestParam String orderStatus) {
        // Now returns the Order object which contains the userId needed for notifications
        Order updatedOrder = orderService.updateStatus(orderId, paymentStatus, orderStatus);
        return ResponseEntity.ok(updatedOrder);
    }
}