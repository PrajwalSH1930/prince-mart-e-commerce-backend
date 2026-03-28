package com.pm.shipment.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long shipmentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    private String carrier; // e.g., "BlueDart", "Delhivery"
    
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "tracking_number")
    private String trackingNumber;

    private String status; // PACKED, SHIPPED, IN_TRANSIT, DELIVERED

    @CreationTimestamp
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    public String getCustomerName() {
    			return customerName;
    }
    public void setCustomerName(String customerName) {
				this.customerName = customerName;
	}
    	public String getCustomerEmail() {
				return customerEmail;
	}
    		public void setCustomerEmail(String customerEmail) {
				this.customerEmail = customerEmail;
	}
	public Long getShipmentId() {
		return shipmentId;
	}

	public void setShipmentId(Long shipmentId) {
		this.shipmentId = shipmentId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getShippedAt() {
		return shippedAt;
	}

	public void setShippedAt(LocalDateTime shippedAt) {
		this.shippedAt = shippedAt;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	

	public Shipment(Long shipmentId, Long orderId, String carrier, String customerName, String customerEmail,
			String trackingNumber, String status, LocalDateTime shippedAt, LocalDateTime deliveredAt) {
		super();
		this.shipmentId = shipmentId;
		this.orderId = orderId;
		this.carrier = carrier;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.trackingNumber = trackingNumber;
		this.status = status;
		this.shippedAt = shippedAt;
		this.deliveredAt = deliveredAt;
	}
	public Shipment() {
		super();
		// TODO Auto-generated constructor stub
	}
}