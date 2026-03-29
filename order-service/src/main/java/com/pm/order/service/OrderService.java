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
    private final UserClient userClient;
    private final ShippingClient shippingClient; 
    private final CouponClient couponClient;

    public OrderService(OrderRepository orderRepository,
                        OrderStatusHistoryRepository historyRepository,
                        CartClient cartClient,
                        InventoryClient inventoryClient,
                        AddressClient addressClient,
                        PaymentClient paymentClient,
                        UserClient userClient,
                        ShippingClient shippingClient,
                        CouponClient couponClient) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.cartClient = cartClient;
        this.inventoryClient = inventoryClient;
        this.addressClient = addressClient;
        this.paymentClient = paymentClient;
        this.userClient = userClient;
        this.shippingClient = shippingClient;
        this.couponClient = couponClient;
    }

    @Transactional
    public Order placeOrder(Long userId, OrderRequest request) {
        CartResponse cart = cartClient.getMyCart(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new ResourceNotFoundException("Cannot place order: Cart is empty");
        }

        UserResponse user = userClient.getUserById(userId);
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

        Order order = new Order();
        order.setUserId(userId);
        order.setCustomerName(user.getFullName());
        order.setUserEmail(user.getEmail());
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

        // Calculate Totals and Apply Coupon
        BigDecimal cartTotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCoupon = request.getCouponCode();

        if (appliedCoupon != null && !appliedCoupon.isEmpty()) {
            try {
                CouponResponse couponRes = couponClient.validateCoupon(userId, 
                    new CouponRequest(appliedCoupon, cartTotal));
                
                if (couponRes.isValid()) {
                    discountAmount = couponRes.getDiscountAmount();
                }
            } catch (Exception e) {
                System.err.println("Coupon validation failed: " + e.getMessage());
            }
        }

        order.setTotalAmount(cartTotal.subtract(discountAmount));

        // SAVE AND FLUSH: This makes the order visible to the Coupon Service's verify call
        Order savedOrder = orderRepository.save(order);
        orderRepository.flush(); 

        saveHistory(savedOrder, "PENDING", "SYSTEM");

        // Record Coupon Usage
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                couponClient.useCoupon(userId, savedOrder.getOrderId(), appliedCoupon);
            } catch (Exception e) {
                System.err.println("Failed to record coupon usage: " + e.getMessage());
            }
        }

        // Inventory Management
        List<StockUpdateDTO> stockUpdates = savedOrder.getItems().stream()
                .map(item -> new StockUpdateDTO(item.getVariantId(), item.getQuantity()))
                .collect(Collectors.toList());

        try {
            inventoryClient.reduceStock(stockUpdates);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Inventory update failed: " + e.getMessage());
        }

        try {
            cartClient.clearCart(userId);
        } catch (Exception e) {
            System.err.println("Non-critical error: Failed to clear cart - " + e.getMessage());
        }

        // Payment Processing
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
            } else {
                handlePaymentFailure(savedOrder, stockUpdates, "Payment Rejected");
            }
        } catch (Exception e) {
            handlePaymentFailure(savedOrder, stockUpdates, "Payment Connection Error: " + e.getMessage());
        }

        return savedOrder;
    }

    private void handlePaymentFailure(Order order, List<StockUpdateDTO> stockUpdates, String reason) {
        order.setPaymentStatus("FAILED");
        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);
        saveHistory(order, "ORDER_CANCELLED_PAYMENT_FAILURE", "SYSTEM");
        try {
            inventoryClient.addStock(stockUpdates);
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to restore stock");
        }
    }

    private void saveHistory(Order order, String status, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        historyRepository.save(history);
    }

    @Transactional
    public Order updateStatus(Long orderId, String paymentStatus, String orderStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setPaymentStatus(paymentStatus);
        order.setOrderStatus(orderStatus);
        Order updatedOrder = orderRepository.save(order);
        saveHistory(updatedOrder, orderStatus, "PAYMENT_SERVICE");

        if ("CONFIRMED".equalsIgnoreCase(orderStatus.trim())) {
            try {
                shippingClient.initiateShipment(orderId, updatedOrder.getCustomerName(), updatedOrder.getUserEmail());
            } catch (Exception e) {
                System.err.println("Shipping Trigger Failed");
            }
        }
        return updatedOrder; 
    }

    public List<Order> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderDetails(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Unauthorized");
        }
        return order;
    }
    
    public boolean checkUserPurchasedProduct(Long userId, Long productId) {
        return orderRepository.existsByUserIdAndProductId(userId, productId);
    }
}