package com.pm.shipment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.shipment.client.AuditClient; // New client
import com.pm.shipment.client.NotificationClient;
import com.pm.shipment.dto.AuditLogRequest; // New DTO
import com.pm.shipment.dto.NotificationRequest;
import com.pm.shipment.entity.Shipment;
import com.pm.shipment.exception.ResourceNotFoundException;
import com.pm.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final NotificationClient notificationClient;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public ShipmentService(ShipmentRepository shipmentRepository, 
                           NotificationClient notificationClient,
                           AuditClient auditClient,
                           ObjectMapper objectMapper) {
        this.shipmentRepository = shipmentRepository;
        this.notificationClient = notificationClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
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
                    
                    // Audit the creation of the shipment record
                    sendAuditLog(null, "SHIPMENT_CREATED", "Order ID: " + orderId, saved);

                    // Initial notification
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
        
        // Capture state before status change
        String dataBefore = null;
        try {
            dataBefore = objectMapper.writeValueAsString(shipment);
        } catch (Exception e) {}

        shipment.setStatus(status.toUpperCase());
        
        if ("DELIVERED".equalsIgnoreCase(status)) {
            shipment.setDeliveredAt(java.time.LocalDateTime.now());
        }
        
        Shipment updatedShipment = shipmentRepository.save(shipment);

        // Audit the status transition
        sendAuditLog(null, "SHIPMENT_STATUS_UPDATE", dataBefore, updatedShipment);

        // Trigger Email Notification
        sendShippingEmail(updatedShipment);
        
        return updatedShipment;
    }

    private void sendShippingEmail(Shipment shipment) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setRecipient(shipment.getCustomerEmail());
            request.setCustomerName(shipment.getCustomerName());
            request.setOrderId(shipment.getOrderId().toString());
            
            String subject = "Prince Mart Update: Order #" + shipment.getOrderId() + " is " + shipment.getStatus();
            request.setSubject(subject);
            request.setAmount(shipment.getStatus()); 

            notificationClient.sendOrderConfirmation(request);
            
            // Log that a shipping notification was triggered
            sendAuditLog(null, "SHIPPING_NOTIFICATION_TRIGGERED", "Status: " + shipment.getStatus(), "Recipient: " + shipment.getCustomerEmail());
            
            System.out.println("DEBUG: Shipping Email Triggered for Status: " + shipment.getStatus());
        } catch (Exception e) {
            System.err.println("Non-critical: Failed to send shipping email: " + e.getMessage());
        }
    }

    // Centralized Helper for External Auditing
    private void sendAuditLog(Long userId, String action, Object dataBefore, Object dataAfter) {
        try {
            String before = dataBefore != null ? (dataBefore instanceof String ? (String) dataBefore : objectMapper.writeValueAsString(dataBefore)) : null;
            String after = dataAfter != null ? (dataAfter instanceof String ? (String) dataAfter : objectMapper.writeValueAsString(dataAfter)) : null;

            auditClient.createLog(new AuditLogRequest(
                "SHIPMENT-SERVICE", 
                action, 
                userId, 
                before, 
                after
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed in Shipment Service: " + e.getMessage());
        }
    }
    
    public List<Shipment> getAllShipments() {
		return shipmentRepository.findAll();
	}
}