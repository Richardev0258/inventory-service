package com.admincore.microservice.inventory.service.impl;

import com.admincore.microservice.inventory.client.ProductServiceClient;
import com.admincore.microservice.inventory.dto.InventoryRequest;
import com.admincore.microservice.inventory.dto.InventoryResponse;
import com.admincore.microservice.inventory.dto.PurchaseRequest;
import com.admincore.microservice.inventory.dto.PurchaseResponse;
import com.admincore.microservice.inventory.exception.InsufficientInventoryException;
import com.admincore.microservice.inventory.exception.InventoryNotFoundException;
import com.admincore.microservice.inventory.model.Inventory;
import com.admincore.microservice.inventory.repository.InventoryRepository;
import com.admincore.microservice.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional
    public InventoryResponse createOrUpdateInventory(InventoryRequest request) {
        log.info("Creating or updating inventory for product ID: {}", request.getProductId());

        // Validar que el producto exista en el servicio de productos
        if (!productServiceClient.isProductAvailable(request.getProductId())) {
            throw new IllegalArgumentException("Product with ID " + request.getProductId() + " does not exist in the product service");
        }

        // Buscar si ya existe un registro de inventario para este producto
        return inventoryRepository.findByProductId(request.getProductId())
                .map(existingInventory -> {
                    // Si existe, actualizar la cantidad
                    existingInventory.setQuantity(request.getQuantity());
                    Inventory savedInventory = inventoryRepository.save(existingInventory);
                    log.info("Updated inventory for product ID: {}", request.getProductId());
                    return toResponse(savedInventory);
                })
                .orElseGet(() -> {
                    // Si no existe, crear uno nuevo
                    Inventory newInventory = new Inventory();
                    newInventory.setProductId(request.getProductId());
                    newInventory.setQuantity(request.getQuantity());
                    Inventory savedInventory = inventoryRepository.save(newInventory);
                    log.info("Created new inventory for product ID: {}", request.getProductId());
                    return toResponse(savedInventory);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        log.info("Fetching inventory for product ID: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .map(this::toResponse)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product ID: " + productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventories() {
        log.info("Fetching all inventories");
        return inventoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseResponse purchase(PurchaseRequest request) {
        log.info("Processing purchase for product ID: {} with quantity: {}", request.getProductId(), request.getQuantity());

        // 1. Validar que el producto exista en el servicio de productos
        if (!productServiceClient.isProductAvailable(request.getProductId())) {
            throw new IllegalArgumentException("Product with ID " + request.getProductId() + " does not exist");
        }

        // 2. Verificar disponibilidad en inventario
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product ID: " + request.getProductId()));

        if (inventory.getQuantity() < request.getQuantity()) {
            throw new InsufficientInventoryException(
                    "Insufficient inventory for product ID: " + request.getProductId() +
                            ". Available: " + inventory.getQuantity() + ", Requested: " + request.getQuantity()
            );
        }

        // 3. Actualizar inventario (descontar cantidad)
        int newQuantity = inventory.getQuantity() - request.getQuantity();
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
        log.info("Updated inventory for product ID: {}. New quantity: {}", request.getProductId(), newQuantity);

        // 4. Preparar respuesta
        String productName = productServiceClient.getProductName(request.getProductId());
        PurchaseResponse response = new PurchaseResponse();
        response.setProductId(request.getProductId());
        response.setProductName(productName);
        response.setPurchasedQuantity(request.getQuantity());
        response.setMessage("Purchase successful. " + request.getQuantity() + " units of '" + productName + "' purchased.");

        return response;
    }

    private InventoryResponse toResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setProductId(inventory.getProductId());
        response.setQuantity(inventory.getQuantity());
        return response;
    }
}