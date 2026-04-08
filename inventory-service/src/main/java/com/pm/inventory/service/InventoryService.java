package com.pm.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.inventory.client.AuditClient;
import com.pm.inventory.dto.AuditLogRequest;
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
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public InventoryService(InventoryRepository inventoryRepository, 
                            TransactionRepository transactionRepository,
                            WarehouseRepository warehouseRepository,
                            AuditClient auditClient,
                            ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.warehouseRepository = warehouseRepository;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Inventory addStock(Long productId, Long variantId, Long warehouseId, Integer quantity, String reference) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));

        Inventory inventory = inventoryRepository.findByVariantIdAndWarehouse_WarehouseId(variantId, warehouseId)
                .orElse(new Inventory());

        // Capture data before change
        String dataBefore = null;
        if (inventory.getInventoryId() != null) {
            dataBefore = "Quantity: " + inventory.getAvailableQuantity();
        }

        if (inventory.getInventoryId() == null) {
            inventory.setProductId(productId);
            inventory.setVariantId(variantId);
            inventory.setWarehouse(warehouse);
            inventory.setAvailableQuantity(quantity);
        } else {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        }

        Inventory savedInventory = inventoryRepository.save(inventory);

        // Audit Trail for Purchase (Internal)
        logTransaction(productId, variantId, quantity, reference, InventoryTransaction.TransactionType.PURCHASE);
        
        // Audit Log (External)
        sendAuditLog(null, "ADD_STOCK", dataBefore, savedInventory);

        return savedInventory;
    }

    @Transactional
    public void reduceStock(List<StockUpdateDTO> updates) {
        for (StockUpdateDTO update : updates) {
            Inventory inventory = inventoryRepository.findByVariantId(update.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found in inventory: " + update.getVariantId()));

            String dataBefore = "Quantity: " + inventory.getAvailableQuantity();

            if (inventory.getAvailableQuantity() < update.getQuantity()) {
                throw new ResourceNotFoundException("Insufficient stock for variant ID: " + update.getVariantId() 
                    + ". Available: " + inventory.getAvailableQuantity());
            }

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - update.getQuantity());
            Inventory savedInventory = inventoryRepository.save(inventory);

            // Audit Trail for Sale
            logTransaction(inventory.getProductId(), update.getVariantId(), update.getQuantity(), "ORDER_PLACE", InventoryTransaction.TransactionType.SALE);
            
            // External Audit Log
            sendAuditLog(null, "REDUCE_STOCK", dataBefore, savedInventory);
        }
    }

    @Transactional
    public void addStockBulk(List<StockUpdateDTO> updates) {
        for (StockUpdateDTO update : updates) {
            Inventory inventory = inventoryRepository.findByVariantId(update.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found for restoration: " + update.getVariantId()));

            String dataBefore = "Quantity: " + inventory.getAvailableQuantity();

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + update.getQuantity());
            Inventory savedInventory = inventoryRepository.save(inventory);

            // Audit Trail for Restoration
            logTransaction(inventory.getProductId(), update.getVariantId(), update.getQuantity(), "PAYMENT_FAILURE_RESTORE", InventoryTransaction.TransactionType.PURCHASE);
            
            // External Audit Log
            sendAuditLog(null, "RESTORE_STOCK_BULK", dataBefore, savedInventory);
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

    // Centralized Helper for External Auditing
    private void sendAuditLog(Long userId, String action, Object dataBefore, Object dataAfter) {
        try {
            String before = dataBefore != null ? (dataBefore instanceof String ? (String) dataBefore : objectMapper.writeValueAsString(dataBefore)) : null;
            String after = dataAfter != null ? (dataAfter instanceof String ? (String) dataAfter : objectMapper.writeValueAsString(dataAfter)) : null;

            auditClient.createLog(new AuditLogRequest(
                "INVENTORY-SERVICE", 
                action, 
                userId, 
                before, 
                after
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed in Inventory Service: " + e.getMessage());
        }
    }
    
    public List<Inventory> getInventoryByProductIdAndVariantId(Long productId, Long variantId) {
		return inventoryRepository.findByProductIdAndVariantId(productId, variantId);
	}

	public List<Inventory> getAllInventories() {
		// TODO Auto-generated method stub
		return inventoryRepository.findAll();
	}
	
	public List<Warehouse> getAllWarehouses() {
		// TODO Auto-generated method stub
		return warehouseRepository.findAll();
	}
}