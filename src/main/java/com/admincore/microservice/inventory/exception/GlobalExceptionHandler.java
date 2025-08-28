package com.admincore.microservice.inventory.exception;

import com.admincore.microservice.inventory.dto.JsonApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<JsonApiResponse<Object>> handleNotFound(InventoryNotFoundException ex) {
        log.warn("Inventory not found: {}", ex.getMessage());
        Map<String, Object> error = createErrorMap("404", "Inventory Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new JsonApiResponse<>(List.of(error)));
    }

    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<JsonApiResponse<Object>> handleInsufficientInventory(InsufficientInventoryException ex) {
        log.warn("Insufficient inventory: {}", ex.getMessage());
        Map<String, Object> error = createErrorMap("400", "Insufficient Inventory", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JsonApiResponse<>(List.of(error)));
    }

    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<JsonApiResponse<Object>> handleProductServiceError(ProductServiceException ex) {
        log.error("Product service error: {}", ex.getMessage());
        Map<String, Object> error = createErrorMap(String.valueOf(ex.getStatus().value()), "Product Service Error", ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(new JsonApiResponse<>(List.of(error)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        List<Map<String, Object>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> createErrorMap("400", "Validation Error", error.getField() + ": " + error.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JsonApiResponse<>(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiResponse<Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        Map<String, Object> error = createErrorMap("500", "Internal Server Error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonApiResponse<>(List.of(error)));
    }

    private Map<String, Object> createErrorMap(String status, String title, String detail) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("title", title);
        error.put("detail", detail);
        return error;
    }
}