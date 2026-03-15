package com.pm.order.service;

import com.pm.order.client.CartClient;
import com.pm.order.client.InventoryClient; 
import com.pm.order.dto.CartResponse;
import com.pm.order.dto.OrderRequest;
import com.pm.order.dto.StockUpdateDTO;
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

    public OrderService(OrderRepository orderRepository, 
                        OrderStatusHistoryRepository historyRepository, 
                        CartClient cartClient,
                        InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.cartClient = cartClient;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public Order placeOrder(Long userId, OrderRequest request) {
        // 1. Fetch Cart from Cart Service via Feign
        CartResponse cart = cartClient.getMyCart(userId);
        
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order: Cart is empty");
        }

        // 2. Create Order Entity
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        order.setCurrency(request.getCurrency());
        order.setShippingAddressId(request.getShippingAddressId());
        order.setBillingAddressId(request.getBillingAddressId());

        // 3. Map Cart Items to Order Items
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(cartItem.getProductId());
            item.setVariantId(cartItem.getVariantId());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPriceSnapshot());
            
            // Calculate subtotal: price * quantity
            item.setSubtotal(cartItem.getPriceSnapshot().multiply(new BigDecimal(cartItem.getQuantity())));
            return item;
        }).collect(Collectors.toList());

        order.setItems(orderItems);

        // 4. Calculate Total Amount
        BigDecimal total = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        // 5. Save Order (This also saves items because of CascadeType.ALL)
        Order savedOrder = orderRepository.save(order);

        // 6. Record Initial History
        saveHistory(savedOrder, "PENDING", "SYSTEM");

        // 7. Reduce Stock in Inventory Service
        // Map order items to the DTO expected by Inventory Service
        List<StockUpdateDTO> stockUpdates = savedOrder.getItems().stream()
                .map(item -> new StockUpdateDTO(item.getVariantId(), item.getQuantity()))
                .collect(Collectors.toList());

        try {
            inventoryClient.reduceStock(stockUpdates);
        } catch (Exception e) {
            // If stock reduction fails (e.g., insufficient stock), 
            // the @Transactional will rollback Step 5 and 6 automatically.
            throw new RuntimeException("Inventory update failed: " + e.getMessage());
        }
        
        // 8. Clear/Deactivate the cart via Feign
        try {
            cartClient.clearCart(userId);
            System.out.println("Cart cleared successfully for user: " + userId);
        } catch (Exception e) {
            // We log the error but allow the order to proceed as it's already saved and stock is reduced
            System.err.println("Non-critical error: Failed to clear cart - " + e.getMessage());
        }

        return savedOrder;
    }

    private void saveHistory(Order order, String status, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        historyRepository.save(history);
    }
    
    public List<Order> getOrderHistory(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for user: " + userId);
        }
        return orders;
    }
    
    public Order getOrderDetails(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
                
        // Security check: Ensure the order actually belongs to the user asking for it
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to view this order");
        }
        return order;
    }
}