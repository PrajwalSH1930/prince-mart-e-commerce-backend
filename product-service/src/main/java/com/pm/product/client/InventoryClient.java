package com.pm.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pm.product.config.FeignConfig;

// The value matches the spring.application.name of the Inventory Service
@FeignClient(name = "inventory-service", configuration = FeignConfig.class)
public interface InventoryClient {

    @PostMapping("/inventory/add-stock")
    void initializeStock(
            @RequestParam("productId") Long productId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("reference") String reference
    );
}