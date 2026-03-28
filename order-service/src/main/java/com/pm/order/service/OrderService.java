package com.pm.order.service;

import com.pm.order.client.*;
import com.pm.order.dto.*;
import com.pm.order.entity.Order;
import com.pm.order.entity.OrderItem;
import com.pm.order.entity.OrderStatusHistory;
import com.pm.order.exception.ResourceNotFoundException;
import com.pm.order.repository.OrderRepository;
import com.pm.order.repository.OrderStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CartClient cartClient;
    private final InventoryClient inventoryClient;
    private final AddressClient addressClient;
    private final PaymentClient paymentClient;
    private final UserClient userClient; // Added

    public OrderService(OrderRepository orderRepository,
                        OrderStatusHistoryRepository historyRepository,
                        CartClient cartClient,
                        InventoryClient inventoryClient,
                        AddressClient addressClient,
                        PaymentClient paymentClient,
                        UserClient userClient) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.cartClient = cartClient;
        this.inventoryClient = inventoryClient;
        this.addressClient = addressClient;
        this.paymentClient = paymentClient;
        this.userClient = userClient;
    }

    @Transactional
    public Order placeOrder(Long userId, OrderRequest request) {
        // 1. Fetch Cart
        CartResponse cart = cartClient.getMyCart(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order: Cart is empty");
        }

        // 2. Fetch User Details from Identity Service
        UserResponse user = userClient.getUserById(userId);

        // 3. Create Address Snapshot
        AddressResponse address = addressClient.getAddressById(request.getShippingAddressId());
        String snapshot = String.format("%s\n%s, %s\n%s, %s, %s - %s\nPhone: %s",
                address.getFullName(),
                address.getAddressLine1(),
                address.getAddressLine2() != null ? address.getAddressLine2() : "",
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode(),
                address.getPhone());

        // 4. Map Order
        Order order = new Order();
        order.setUserId(userId);
        order.setCustomerName(user.getFullName()); // Saving real name
        order.setUserEmail(user.getEmail());       // Saving real email
        order.setOrderStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        order.setCurrency(request.getCurrency());
        order.setShippingAddressId(request.getShippingAddressId());
        order.setBillingAddressId(request.getBillingAddressId());
        order.setShippingAddressSnapshot(snapshot); 

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(cartItem.getProductId());
            item.setVariantId(cartItem.getVariantId());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPriceSnapshot());
            item.setSubtotal(cartItem.getPriceSnapshot().multiply(new BigDecimal(cartItem.getQuantity())));
            return item;
        }).collect(Collectors.toList());

        order.setItems(orderItems);

        BigDecimal total = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        // 5. Save and Update Stock
        Order savedOrder = orderRepository.save(order);
        saveHistory(savedOrder, "PENDING", "SYSTEM");

        List<StockUpdateDTO> stockUpdates = savedOrder.getItems().stream()
                .map(item -> new StockUpdateDTO(item.getVariantId(), item.getQuantity()))
                .collect(Collectors.toList());

        try {
            inventoryClient.reduceStock(stockUpdates);
        } catch (Exception e) {
            throw new RuntimeException("Inventory update failed: " + e.getMessage());
        }

        // 6. Clear Cart
        try {
            cartClient.clearCart(userId);
        } catch (Exception e) {
            System.err.println("Non-critical error: Failed to clear cart - " + e.getMessage());
        }

        // 7. Request RazorPay Order
        PaymentRequest paymentRequest = new PaymentRequest(
                savedOrder.getOrderId(),
                savedOrder.getTotalAmount(),
                savedOrder.getCurrency(),
                "RAZORPAY" 
        );

        try {
            PaymentResponse paymentResponse = paymentClient.process(paymentRequest);
            
            if ("CREATED".equalsIgnoreCase(paymentResponse.getPaymentStatus()) || 
                "COMPLETED".equalsIgnoreCase(paymentResponse.getPaymentStatus())) {
                
                savedOrder.setTransactionId(paymentResponse.getTransactionId());
                orderRepository.save(savedOrder);
                System.out.println("RazorPay Order Created ID: " + paymentResponse.getTransactionId());
                
            } else {
                handlePaymentFailure(savedOrder, stockUpdates, "Payment Rejected by Gateway");
            }
            
        } catch (Exception e) {
            handlePaymentFailure(savedOrder, stockUpdates, "Payment Service Connection Error: " + e.getMessage());
        }

        return savedOrder;
    }

    private void handlePaymentFailure(Order order, List<StockUpdateDTO> stockUpdates, String reason) {
        System.err.println("Payment failed for order " + order.getOrderId() + ": " + reason);
        order.setPaymentStatus("FAILED");
        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);
        saveHistory(order, "ORDER_CANCELLED_PAYMENT_FAILURE", "SYSTEM");

        try {
            inventoryClient.addStock(stockUpdates);
            System.out.println("Stock restored successfully for order: " + order.getOrderId());
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to restore stock - " + e.getMessage());
        }
    }

    private void saveHistory(Order order, String status, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        historyRepository.save(history);
    }

    public List<Order> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderDetails(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return order;
    }
    
    @Transactional
    public Order updateStatus(Long orderId, String paymentStatus, String orderStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setPaymentStatus(paymentStatus);
        order.setOrderStatus(orderStatus);
        Order updatedOrder = orderRepository.save(order);
        
        saveHistory(updatedOrder, orderStatus, "PAYMENT_SERVICE");
        return updatedOrder; 
    }
}