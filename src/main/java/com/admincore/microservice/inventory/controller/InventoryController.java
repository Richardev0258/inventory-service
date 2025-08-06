package com.admincore.microservice.inventory.controller;

import com.admincore.microservice.inventory.dto.*;
import com.admincore.microservice.inventory.service.impl.InventoryServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryServiceImpl inventoryService;

    @PostMapping
    public ResponseEntity<JsonApiResponse<InventoryResponse>> createOrUpdateInventory(@RequestBody @Valid InventoryRequest request) {
        log.info("Received request to create or update inventory for product ID: {}", request.getProductId());
        InventoryResponse response = inventoryService.createOrUpdateInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JsonApiResponse<>(response));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<JsonApiResponse<InventoryResponse>> getInventoryByProductId(@PathVariable Long productId) {
        log.info("Received request to get inventory for product ID: {}", productId);
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(new JsonApiResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<JsonApiResponse<List<InventoryResponse>>> getAllInventories() {
        log.info("Received request to get all inventories");
        List<InventoryResponse> response = inventoryService.getAllInventories();
        return ResponseEntity.ok(new JsonApiResponse<>(response));
    }

    @PostMapping("/purchases")
    public ResponseEntity<JsonApiResponse<PurchaseResponse>> purchase(@RequestBody @Valid PurchaseRequest request) {
        log.info("Received purchase request for product ID: {} with quantity: {}", request.getProductId(), request.getQuantity());
        PurchaseResponse response = inventoryService.purchase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JsonApiResponse<>(response));
    }
}