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
    
    // This is the endpoint the Order Service will call
    @PostMapping("/initiate/{orderId}")
    public ResponseEntity<Shipment> initiateShipment(@PathVariable Long orderId) {
        System.out.println("Received request to initiate shipment for Order ID: " + orderId);
        Shipment shipment = shipmentService.createShipment(orderId);
        return ResponseEntity.ok(shipment);
    }

    // To check the status of a shipment (for the User/Frontend)
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getShipmentByOrder(@PathVariable Long orderId) {
        return shipmentService.getShipmentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // To manually update status (e.g., from 'PACKING' to 'SHIPPED')
    @PutMapping("/{shipmentId}/status")
    public ResponseEntity<Shipment> updateStatus(
            @PathVariable Long shipmentId, 
            @RequestParam String status) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(shipmentId, status));
    }
}