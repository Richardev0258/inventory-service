package com.admincore.microservice.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear o actualizar un registro de inventario")
public class InventoryRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID del producto", example = "1")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    @Schema(description = "Cantidad disponible del producto", example = "100", minimum = "0")
    private Integer quantity;
}