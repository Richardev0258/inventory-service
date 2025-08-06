package com.admincore.microservice.inventory.integration;

import com.admincore.microservice.inventory.InventoryServiceApplication;
import com.admincore.microservice.inventory.dto.InventoryRequest;
import com.admincore.microservice.inventory.dto.PurchaseRequest;
import com.admincore.microservice.inventory.model.Inventory;
import com.admincore.microservice.inventory.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = InventoryServiceApplication.class)
@ActiveProfiles("test")
@AutoConfigureWebMvc
class InventoryIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        inventoryRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        inventoryRepository.deleteAll();
    }

    @Test
    public void shouldCreateInventorySuccessfully() throws Exception {
        InventoryRequest request = new InventoryRequest();
        request.setProductId(1L);
        request.setQuantity(100);

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productId").value(1L))
                .andExpect(jsonPath("$.data.quantity").value(100));
    }

    @Test
    public void shouldFetchInventoryByProductId() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setQuantity(50);
        inventoryRepository.save(inventory);

        mockMvc.perform(get("/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(1L))
                .andExpect(jsonPath("$.data.quantity").value(50));
    }

    @Test
    public void shouldReturn404ForMissingInventory() throws Exception {
        mockMvc.perform(get("/inventory/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldListAllInventories() throws Exception {
        inventoryRepository.save(new Inventory(null, 1L, 100));
        inventoryRepository.save(new Inventory(null, 2L, 200));

        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].productId").value(1L))
                .andExpect(jsonPath("$.data[1].productId").value(2L));
    }

    @Test
    public void shouldProcessPurchaseSuccessfully() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setQuantity(10);
        inventoryRepository.save(inventory);

        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        mockMvc.perform(post("/inventory/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productId").value(1L))
                .andExpect(jsonPath("$.data.purchasedQuantity").value(3));

        mockMvc.perform(get("/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(7)); // 10 - 3 = 7
    }

    @Test
    public void shouldReturn400ForInsufficientInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setQuantity(2);
        inventoryRepository.save(inventory);

        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        request.setQuantity(5);

        mockMvc.perform(post("/inventory/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}