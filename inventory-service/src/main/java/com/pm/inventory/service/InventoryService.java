package com.pm.inventory.service;

import com.pm.inventory.entity.Inventory;
import com.pm.inventory.entity.InventoryTransaction;
import com.pm.inventory.entity.Warehouse;
import com.pm.inventory.exception.ResourceNotFoundException;
import com.pm.inventory.repository.InventoryRepository;
import com.pm.inventory.repository.TransactionRepository;
import com.pm.inventory.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(InventoryRepository inventoryRepository, 
                            TransactionRepository transactionRepository,
                            WarehouseRepository warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public Inventory addStock(Long productId, Long variantId, Long warehouseId, Integer quantity, String reference) {
        // 1. Validate Warehouse exists
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));

        // 2. Find or Create Inventory Record
        Inventory inventory = inventoryRepository.findByVariantIdAndWarehouse_WarehouseId(variantId, warehouseId)
                .orElse(new Inventory());

        if (inventory.getInventoryId() == null) {
            inventory.setProductId(productId);
            inventory.setVariantId(variantId);
            inventory.setWarehouse(warehouse);
            inventory.setAvailableQuantity(quantity);
        } else {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        }

        Inventory savedInventory = inventoryRepository.save(inventory);

        // 3. Log the Transaction (Audit Trail)
        InventoryTransaction tx = new InventoryTransaction();
        tx.setProductId(productId);
        tx.setVariantId(variantId);
        tx.setType(InventoryTransaction.TransactionType.PURCHASE);
        tx.setQuantity(quantity);
        tx.setReferenceId(reference);
        
        transactionRepository.save(tx);

        return savedInventory;
    }
    
    public Warehouse getWareHouseById(Long id) {
    	return warehouseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));
    }
    
    public Inventory getInventoryById(Long id) {
    	return inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
    }
}