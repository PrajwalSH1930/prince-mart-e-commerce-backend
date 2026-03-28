package com.pm.shipment.service;

import com.pm.shipment.client.NotificationClient;
import com.pm.shipment.dto.NotificationRequest;
import com.pm.shipment.entity.Shipment;
import com.pm.shipment.exception.ResourceNotFoundException;
import com.pm.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final NotificationClient notificationClient;

    public ShipmentService(ShipmentRepository shipmentRepository, NotificationClient notificationClient) {
        this.shipmentRepository = shipmentRepository;
        this.notificationClient = notificationClient;
    }

    public Shipment getStatusByShipmentId(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
    }
    
    @Transactional
    public Shipment createShipment(Long orderId, String customerName, String customerEmail) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    Shipment shipment = new Shipment();
                    shipment.setOrderId(orderId);
                    shipment.setCustomerName(customerName);
                    shipment.setCustomerEmail(customerEmail);
                    shipment.setCarrier("PRINCE-EXPRESS");
                    shipment.setStatus("PACKING");
                    shipment.setTrackingNumber("PM-TRACK-" + System.currentTimeMillis());
                    
                    Shipment saved = shipmentRepository.save(shipment);
                    // Initial notification: "We are packing your order"
                    sendShippingEmail(saved);
                    return saved;
                });
    }

    public Optional<Shipment> getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Shipment updateShipmentStatus(Long shipmentId, String status) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        
        shipment.setStatus(status.toUpperCase());
        
        if ("DELIVERED".equalsIgnoreCase(status)) {
            shipment.setDeliveredAt(java.time.LocalDateTime.now());
        }
        
        Shipment updatedShipment = shipmentRepository.save(shipment);

        // TRIGGER EMAIL: "Your order is SHIPPED / OUT_FOR_DELIVERY / DELIVERED"
        sendShippingEmail(updatedShipment);
        
        return updatedShipment;
    }

    private void sendShippingEmail(Shipment shipment) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setRecipient(shipment.getCustomerEmail());
            request.setCustomerName(shipment.getCustomerName());
            request.setOrderId(shipment.getOrderId().toString());
            
            // Dynamic Subject based on status
            String subject = "Prince Mart Update: Order #" + shipment.getOrderId() + " is " + shipment.getStatus();
            request.setSubject(subject);
            
            // We pass the status into the 'amount' field so Thymeleaf can use it
            request.setAmount(shipment.getStatus()); 

            notificationClient.sendOrderConfirmation(request);
            System.out.println("DEBUG: Shipping Email Triggered for Status: " + shipment.getStatus());
        } catch (Exception e) {
            System.err.println("Non-critical: Failed to send shipping email: " + e.getMessage());
        }
    }
}