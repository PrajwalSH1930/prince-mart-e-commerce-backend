package com.pm.shipment.service;

import com.pm.shipment.entity.Shipment;
import com.pm.shipment.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public Shipment createShipment(Long orderId) {
        // Avoid duplicate shipments for the same order
        return shipmentRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    Shipment shipment = new Shipment();
                    shipment.setOrderId(orderId);
                    shipment.setCarrier("PRINCE-EXPRESS");
                    shipment.setStatus("PACKING");
                    // Generate a unique tracking number
                    shipment.setTrackingNumber("PM-TRACK-" + System.currentTimeMillis());
                    return shipmentRepository.save(shipment);
                });
    }

    public Optional<Shipment> getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Shipment updateShipmentStatus(Long shipmentId, String status) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        
        shipment.setStatus(status);
        
        if ("DELIVERED".equalsIgnoreCase(status)) {
            shipment.setDeliveredAt(java.time.LocalDateTime.now());
        }
        
        return shipmentRepository.save(shipment);
    }
}