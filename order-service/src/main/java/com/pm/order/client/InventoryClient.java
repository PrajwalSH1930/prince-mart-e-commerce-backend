package com.pm.order.client;

import com.pm.order.dto.StockUpdateDTO; // Create this DTO in Order Service too
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {
    
    @PostMapping("/inventory/reduce-stock")
    void reduceStock(@RequestBody List<StockUpdateDTO> updates);
}