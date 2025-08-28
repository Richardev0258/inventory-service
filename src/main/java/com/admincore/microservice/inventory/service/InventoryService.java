package com.admincore.microservice.inventory.service;

import com.admincore.microservice.inventory.dto.InventoryRequest;
import com.admincore.microservice.inventory.dto.InventoryResponse;
import com.admincore.microservice.inventory.dto.PurchaseRequest;
import com.admincore.microservice.inventory.dto.PurchaseResponse;

import java.util.List;

public interface InventoryService {
    InventoryResponse createOrUpdateInventory(InventoryRequest request);
    InventoryResponse getInventoryByProductId(Long productId);
    List<InventoryResponse> getAllInventories();
    PurchaseResponse purchase(PurchaseRequest request);
}