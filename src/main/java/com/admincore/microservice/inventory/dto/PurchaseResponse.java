package com.admincore.microservice.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de la respuesta de una compra procesada")
public class PurchaseResponse {
    @Schema(description = "ID del producto comprado", example = "1")
    private Long productId;

    @Schema(description = "Nombre del producto comprado", example = "Teclado mecánico")
    private String productName;

    @Schema(description = "Cantidad de unidades compradas", example = "2")
    private Integer purchasedQuantity;

    @Schema(description = "Mensaje descriptivo del resultado de la compra", example = "Purchase successful. 2 units of 'Teclado mecánico' purchased.")
    private String message;
}