package com.pm.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pm.inventory.entity.InventoryTransaction;

@Repository
public interface TransactionRepository extends JpaRepository<InventoryTransaction, Long> {
	List<InventoryTransaction> findByVariantId(Long variantId);
}
