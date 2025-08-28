package com.admincore.microservice.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta estándar siguiendo el formato JSON:API")
public class JsonApiResponse<T> {
    @Schema(description = "Datos de la respuesta")
    private T data;

    @Schema(description = "Lista de errores, si los hay")
    private List<Map<String, Object>> errors;

    public JsonApiResponse(T data) {
        this.data = data;
        this.errors = null;
    }

    public JsonApiResponse(List<Map<String, Object>> errors) {
        this.data = null;
        this.errors = errors;
    }
}