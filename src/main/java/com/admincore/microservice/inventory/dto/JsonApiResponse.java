package com.admincore.microservice.inventory.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonApiResponse<T> {
    private T data;
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