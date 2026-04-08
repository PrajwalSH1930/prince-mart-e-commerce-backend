package com.pm.inventory.controller;

import com.pm.inventory.dto.StockUpdateDTO;
import com.pm.inventory.entity.Inventory;
import com.pm.inventory.entity.Warehouse;
import com.pm.inventory.service.InventoryService;
import com.pm.inventory.repository.WarehouseRepository;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final WarehouseRepository warehouseRepository;

    public InventoryController(InventoryService inventoryService, WarehouseRepository warehouseRepository) {
        this.inventoryService = inventoryService;
        this.warehouseRepository = warehouseRepository;
    }
    
    @GetMapping("/welcome")
    public String welcome() {
    	return "Welcome!! This is the Inventory Service for Prince Mart by Prince Inc.";
    }
    
    // 1. Create a Warehouse (Do this first)
    @PostMapping("/warehouse/add")
    public ResponseEntity<Warehouse> addWarehouse(@RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(warehouseRepository.save(warehouse));
    }

    // 2. Add Stock to a Variant
    @PostMapping("/add-stock")
    public ResponseEntity<Inventory> addStock(
            @RequestParam Long productId,
            @RequestParam Long variantId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity,
            @RequestParam String reference) {
        
        return ResponseEntity.ok(inventoryService.addStock(productId, variantId, warehouseId, quantity, reference));
    }
    
    @GetMapping("/warehouse/id/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable("id") Long id) {
    	return ResponseEntity.ok(inventoryService.getWareHouseById(id));
    }
    
    @GetMapping("/id/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable("id") Long id) {
    	return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }
    
    @PostMapping("/reduce-stock")
    public ResponseEntity<String> reduceStock(@RequestBody List<StockUpdateDTO> updates) {
        inventoryService.reduceStock(updates);
        return ResponseEntity.ok("Stock updated successfully");
    }
    
    @PostMapping("/add-stock-bulk")
    public ResponseEntity<String> addStockBulk(@RequestBody List<StockUpdateDTO> updates) {
        inventoryService.addStockBulk(updates);
        return ResponseEntity.ok("Stock restored successfully");
    }
    
    @GetMapping("/product/{productId}/variant/{variantId}")
    public List<ResponseEntity<Inventory>> getInventoryByProductAndVariant(
			@PathVariable Long productId,
			@PathVariable Long variantId) {
    			return inventoryService.getInventoryByProductIdAndVariantId(productId, variantId)
				.stream()
				.map(ResponseEntity::ok)
				.toList();
	}
    
    @GetMapping("/all")
    public ResponseEntity<List<Inventory>> getAllInventories() {
		return ResponseEntity.ok(inventoryService.getAllInventories());
	}
    
    @GetMapping("/warehouse/all")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
    			return ResponseEntity.ok(inventoryService.getAllWarehouses());
    }
}