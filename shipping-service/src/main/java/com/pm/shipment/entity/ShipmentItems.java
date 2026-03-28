package com.pm.shipment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shipment_items")
public class ShipmentItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_item_id")
    private Long shipmentItemId;

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @Column(name = "order_item_id")
    private Long orderItemId; // Refers to the ID in Order Service

    private Integer quantity;

	public Long getShipmentItemId() {
		return shipmentItemId;
	}

	public void setShipmentItemId(Long shipmentItemId) {
		this.shipmentItemId = shipmentItemId;
	}

	public Shipment getShipment() {
		return shipment;
	}

	public void setShipment(Shipment shipment) {
		this.shipment = shipment;
	}

	public Long getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(Long orderItemId) {
		this.orderItemId = orderItemId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public ShipmentItems() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ShipmentItems(Long shipmentItemId, Shipment shipment, Long orderItemId, Integer quantity) {
		super();
		this.shipmentItemId = shipmentItemId;
		this.shipment = shipment;
		this.orderItemId = orderItemId;
		this.quantity = quantity;
	}
    
}

