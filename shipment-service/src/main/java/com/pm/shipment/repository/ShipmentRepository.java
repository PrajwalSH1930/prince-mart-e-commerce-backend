package com.pm.shipment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pm.shipment.entity.Shipment;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long>{
	Optional<Shipment> findByOrderId(Long orderId);
}
