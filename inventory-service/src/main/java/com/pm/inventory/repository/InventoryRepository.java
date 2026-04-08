package com.pm.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pm.inventory.entity.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	Optional<Inventory> findByVariantIdAndWarehouse_WarehouseId(Long variantId, Long warehouseId);
	Optional<Inventory> findByVariantId(Long variantId);
	
	List<Inventory> findByProductIdAndVariantId(Long productId, Long variantId);
}
