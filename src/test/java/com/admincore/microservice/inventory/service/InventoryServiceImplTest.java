package com.admincore.microservice.inventory.service;

import com.admincore.microservice.inventory.client.ProductServiceClient;
import com.admincore.microservice.inventory.dto.InventoryRequest;
import com.admincore.microservice.inventory.dto.PurchaseRequest;
import com.admincore.microservice.inventory.exception.InsufficientInventoryException;
import com.admincore.microservice.inventory.exception.InventoryNotFoundException;
import com.admincore.microservice.inventory.model.Inventory;
import com.admincore.microservice.inventory.repository.InventoryRepository;
import com.admincore.microservice.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory;
    private InventoryRequest inventoryRequest;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(100L);
        inventory.setQuantity(10);

        inventoryRequest = new InventoryRequest();
        inventoryRequest.setProductId(100L);
        inventoryRequest.setQuantity(10);
    }

    @Test
    void createOrUpdateInventory_ShouldCreateNewInventory_WhenInventoryDoesNotExist() {
        // Arrange
        when(productServiceClient.isProductAvailable(100L)).thenReturn(true);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // Act
        var result = inventoryService.createOrUpdateInventory(inventoryRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void createOrUpdateInventory_ShouldUpdateExistingInventory_WhenInventoryExists() {
        // Arrange
        Inventory existingInventory = new Inventory();
        existingInventory.setId(1L);
        existingInventory.setProductId(100L);
        existingInventory.setQuantity(5);

        when(productServiceClient.isProductAvailable(100L)).thenReturn(true);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(existingInventory);

        // Act
        var result = inventoryService.createOrUpdateInventory(inventoryRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getProductId());
        assertEquals(10, result.getQuantity()); // Updated quantity
        verify(inventoryRepository, times(1)).save(existingInventory);
    }

    @Test
    void createOrUpdateInventory_ShouldThrowException_WhenProductDoesNotExist() {
        // Arrange
        when(productServiceClient.isProductAvailable(100L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.createOrUpdateInventory(inventoryRequest);
        });

        assertEquals("Product with ID 100 does not exist in the product service", exception.getMessage());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void getInventoryByProductId_ShouldReturnInventory_WhenInventoryExists() {
        // Arrange
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(inventory));

        // Act
        var result = inventoryService.getInventoryByProductId(100L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getProductId());
        assertEquals(10, result.getQuantity());
    }

    @Test
    void getInventoryByProductId_ShouldThrowException_WhenInventoryDoesNotExist() {
        // Arrange
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.empty());

        // Act & Assert
        InventoryNotFoundException exception = assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.getInventoryByProductId(100L);
        });

        assertEquals("Inventory not found for product ID: 100", exception.getMessage());
    }

    @Test
    void purchase_ShouldProcessPurchase_WhenSufficientInventory() {
        // Arrange
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setProductId(100L);
        purchaseRequest.setQuantity(3);

        when(productServiceClient.isProductAvailable(100L)).thenReturn(true);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(inventory));
        when(productServiceClient.getProductName(100L)).thenReturn("Test Product");
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = inventoryService.purchase(purchaseRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getProductId());
        assertEquals("Test Product", result.getProductName());
        assertEquals(3, result.getPurchasedQuantity());
        assertEquals(7, inventory.getQuantity()); // 10 - 3 = 7
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void purchase_ShouldThrowException_WhenProductDoesNotExist() {
        // Arrange
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setProductId(100L);
        purchaseRequest.setQuantity(3);

        when(productServiceClient.isProductAvailable(100L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.purchase(purchaseRequest);
        });

        assertEquals("Product with ID 100 does not exist", exception.getMessage());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void purchase_ShouldThrowException_WhenInventoryDoesNotExist() {
        // Arrange
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setProductId(100L);
        purchaseRequest.setQuantity(3);

        when(productServiceClient.isProductAvailable(100L)).thenReturn(true);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.empty());

        // Act & Assert
        InventoryNotFoundException exception = assertThrows(InventoryNotFoundException.class, () -> {
            inventoryService.purchase(purchaseRequest);
        });

        assertEquals("Inventory not found for product ID: 100", exception.getMessage());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void purchase_ShouldThrowException_WhenInsufficientInventory() {
        // Arrange
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setProductId(100L);
        purchaseRequest.setQuantity(15); // More than available (10)

        when(productServiceClient.isProductAvailable(100L)).thenReturn(true);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(inventory));

        // Act & Assert
        InsufficientInventoryException exception = assertThrows(InsufficientInventoryException.class, () -> {
            inventoryService.purchase(purchaseRequest);
        });

        assertTrue(exception.getMessage().contains("Insufficient inventory"));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }
}