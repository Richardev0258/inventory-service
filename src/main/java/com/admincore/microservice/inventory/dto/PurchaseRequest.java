package com.admincore.microservice.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para procesar una compra")
public class PurchaseRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID del producto a comprar", example = "1")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Cantidad a comprar", example = "2", minimum = "1")
    private Integer quantity;
}