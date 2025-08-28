package com.admincore.microservice.inventory.controller;

import com.admincore.microservice.inventory.dto.*;
import com.admincore.microservice.inventory.service.impl.InventoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "inventory", description = "Operaciones relacionadas con el inventario de productos")
@SecurityRequirement(name = "X-API-KEY")
public class InventoryController {

    private final InventoryServiceImpl inventoryService;

    @PostMapping
    @Operation(
            summary = "Crear o actualizar inventario",
            description = "Crea un nuevo registro de inventario o actualiza la cantidad de uno existente para un producto específico. Verifica que el producto exista en el Product Service."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inventario creado o actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en la solicitud",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "El producto no existe en el Product Service",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class)))
    })
    public ResponseEntity<JsonApiResponse<InventoryResponse>> createOrUpdateInventory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del inventario a crear o actualizar",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InventoryRequest.class))
            )
            @Valid @RequestBody InventoryRequest request) {
        log.info("Received request to create or update inventory for product ID: {}", request.getProductId());
        InventoryResponse response = inventoryService.createOrUpdateInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JsonApiResponse<>(response));
    }

    @GetMapping("/{productId}")
    @Operation(
            summary = "Obtener inventario por ID de producto",
            description = "Obtiene la cantidad disponible de un producto específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado para el ID de producto proporcionado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class)))
    })
    public ResponseEntity<JsonApiResponse<InventoryResponse>> getInventoryByProductId(
            @Parameter(in = ParameterIn.PATH, description = "ID del producto", required = true, schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long productId) {
        log.info("Received request to get inventory for product ID: {}", productId);
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(new JsonApiResponse<>(response));
    }

    @GetMapping
    @Operation(
            summary = "Listar todos los inventarios",
            description = "Obtiene una lista de todos los registros de inventario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inventarios obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class)))
    })
    public ResponseEntity<JsonApiResponse<List<InventoryResponse>>> getAllInventories() {
        log.info("Received request to get all inventories");
        List<InventoryResponse> response = inventoryService.getAllInventories();
        return ResponseEntity.ok(new JsonApiResponse<>(response));
    }

    @PostMapping("/purchases")
    @Operation(
            summary = "Procesar una compra",
            description = """
            Procesa una compra de un producto.
            
            1. Verifica que el producto exista en el Product Service.
            2. Verifica que haya suficiente stock disponible.
            3. Descuenta la cantidad comprada del inventario.
            4. Retorna la información de la compra realizada.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compra procesada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (producto no encontrado, stock insuficiente)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado para el producto",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonApiResponse.class)))
    })
    public ResponseEntity<JsonApiResponse<PurchaseResponse>> purchase(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la compra",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseRequest.class))
            )
            @Valid @RequestBody PurchaseRequest request) {
        log.info("Received purchase request for product ID: {} with quantity: {}", request.getProductId(), request.getQuantity());
        PurchaseResponse response = inventoryService.purchase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JsonApiResponse<>(response));
    }
}