package com.pm.inventory.service;

import com.pm.inventory.dto.StockUpdateDTO;
import com.pm.inventory.entity.Inventory;
import com.pm.inventory.entity.InventoryTransaction;
import com.pm.inventory.entity.Warehouse;
import com.pm.inventory.exception.ResourceNotFoundException;
import com.pm.inventory.repository.InventoryRepository;
import com.pm.inventory.repository.TransactionRepository;
import com.pm.inventory.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));

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

        // Audit Trail for Purchase
        logTransaction(productId, variantId, quantity, reference, InventoryTransaction.TransactionType.PURCHASE);

        return savedInventory;
    }

    @Transactional
    public void reduceStock(List<StockUpdateDTO> updates) {
        for (StockUpdateDTO update : updates) {
            Inventory inventory = inventoryRepository.findByVariantId(update.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Product variant not found in inventory: " + update.getVariantId()));

            if (inventory.getAvailableQuantity() < update.getQuantity()) {
                throw new RuntimeException("Insufficient stock for variant ID: " + update.getVariantId() 
                    + ". Available: " + inventory.getAvailableQuantity());
            }

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - update.getQuantity());
            inventoryRepository.save(inventory);

            // Audit Trail for Sale
            logTransaction(inventory.getProductId(), update.getVariantId(), update.getQuantity(), "ORDER_PLACE", InventoryTransaction.TransactionType.SALE);
        }
    }

    /**
     * NEW: Compensating Transaction logic to restore stock when payment fails.
     */
    @Transactional
    public void addStockBulk(List<StockUpdateDTO> updates) {
        for (StockUpdateDTO update : updates) {
            Inventory inventory = inventoryRepository.findByVariantId(update.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Product variant not found for restoration: " + update.getVariantId()));

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + update.getQuantity());
            inventoryRepository.save(inventory);

            // Audit Trail for Restoration (using PURCHASE or ADJUSTMENT type)
            logTransaction(inventory.getProductId(), update.getVariantId(), update.getQuantity(), "PAYMENT_FAILURE_RESTORE", InventoryTransaction.TransactionType.PURCHASE);
        }
    }

    private void logTransaction(Long productId, Long variantId, Integer quantity, String reference, InventoryTransaction.TransactionType type) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setProductId(productId);
        tx.setVariantId(variantId);
        tx.setType(type);
        tx.setQuantity(quantity);
        tx.setReferenceId(reference);
        transactionRepository.save(tx);
    }

    public Warehouse getWareHouseById(Long id) {
        return warehouseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
    }

    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }
}