package com.pm.shipment.controller;

import com.pm.shipment.entity.Shipment;
import com.pm.shipment.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome!! This is the Shipment Service for Prince Mart by Prince Inc.";
    }
    
    @PostMapping("/initiate/{orderId}")
    public ResponseEntity<Shipment> initiateShipment(
            @PathVariable Long orderId,
            @RequestParam String customerName,
            @RequestParam String customerEmail) {
        
        System.out.println("Initiating shipment for Order ID: " + orderId + " (Customer: " + customerName + ")");
        Shipment shipment = shipmentService.createShipment(orderId, customerName, customerEmail);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getShipmentByOrder(@PathVariable Long orderId) {
        return shipmentService.getShipmentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{shipmentId}")
    public ResponseEntity<Shipment> getShipmentStatus(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(shipmentService.getStatusByShipmentId(shipmentId));
    }

    @PutMapping("/{shipmentId}/status")
    public ResponseEntity<Shipment> updateStatus(
            @PathVariable Long shipmentId, 
            @RequestParam String status) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(shipmentId, status));
    }
    
    @GetMapping("/all")
    public ResponseEntity<Iterable<Shipment>> getAllShipments() {
		return ResponseEntity.ok(shipmentService.getAllShipments());
	}
}