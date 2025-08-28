package com.admincore.microservice.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de un registro de inventario")
public class InventoryResponse {
    @Schema(description = "ID único del registro de inventario", example = "1")
    private Long id;

    @Schema(description = "ID del producto asociado", example = "1")
    private Long productId;

    @Schema(description = "Cantidad disponible del producto", example = "100")
    private Integer quantity;
}