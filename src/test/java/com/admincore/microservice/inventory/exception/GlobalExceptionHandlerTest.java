package com.admincore.microservice.inventory.exception;

import com.admincore.microservice.inventory.dto.JsonApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturn404WithErrorMessage() {
        String message = "Inventory with ID 1 not found";
        InventoryNotFoundException ex = new InventoryNotFoundException(message);

        ResponseEntity<JsonApiResponse<Object>> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getErrors());
        assertFalse(response.getBody().getErrors().isEmpty());

        Map<String, Object> error = response.getBody().getErrors().get(0);
        assertEquals("404", error.get("status"));
        assertEquals("Inventory Not Found", error.get("title"));
        assertEquals(message, error.get("detail"));
    }

    @Test
    void handleInsufficientInventory_shouldReturn400WithErrorMessage() {
        String message = "Not enough items in stock";
        InsufficientInventoryException ex = new InsufficientInventoryException(message);

        ResponseEntity<JsonApiResponse<Object>> response = handler.handleInsufficientInventory(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getErrors());
        assertFalse(response.getBody().getErrors().isEmpty());

        Map<String, Object> error = response.getBody().getErrors().get(0);
        assertEquals("400", error.get("status"));
        assertEquals("Insufficient Inventory", error.get("title"));
        assertEquals(message, error.get("detail"));
    }

    @Test
    void handleGeneric_shouldReturn500WithErrorMessage() {
        String message = "Unexpected error occurred";
        Exception ex = new Exception(message);

        ResponseEntity<JsonApiResponse<Object>> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getErrors());
        assertFalse(response.getBody().getErrors().isEmpty());

        Map<String, Object> error = response.getBody().getErrors().get(0);
        assertEquals("500", error.get("status"));
        assertEquals("Internal Server Error", error.get("title"));
        assertEquals(message, error.get("detail"));
    }

    @Test
    void handleProductServiceError_shouldReturnCorrectStatusAndMessage() {
        String message = "Product service unavailable";
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        ProductServiceException ex = new ProductServiceException(message, status);

        ResponseEntity<JsonApiResponse<Object>> response = handler.handleProductServiceError(ex);

        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getErrors());
        assertFalse(response.getBody().getErrors().isEmpty());

        Map<String, Object> error = response.getBody().getErrors().get(0);
        assertEquals(String.valueOf(status.value()), error.get("status"));
        assertEquals("Product Service Error", error.get("title"));
        assertEquals(message, error.get("detail"));
    }

    @Test
    void handleValidationErrors_shouldReturn400WithValidationMessages() throws NoSuchMethodException {
        @lombok.Getter
        @lombok.Setter
        class TestObject {
            @NotNull(message = "Name is required")
            private String name;

            @Positive(message = "Price must be positive")
            private Double price = -10.0;
        }

        TestObject obj = new TestObject();
        obj.setName(null);
        obj.setPrice(-10.0);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<TestObject>> violations = validator.validate(obj);

            org.springframework.validation.BeanPropertyBindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(obj, "testObject");
            for (ConstraintViolation<TestObject> violation : violations) {
                bindingResult.addError(new org.springframework.validation.FieldError(
                        "testObject",
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        false,
                        new String[]{violation.getMessageTemplate()},
                        new Object[]{},
                        violation.getMessage()
                ));
            }

            java.lang.reflect.Method method = this.getClass().getDeclaredMethod("handleValidationErrors_shouldReturn400WithValidationMessages");
            org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(method, -1);

            MethodArgumentNotValidException validationEx = new MethodArgumentNotValidException(methodParameter, bindingResult);

            ResponseEntity<JsonApiResponse<Object>> response = handler.handleValidationErrors(validationEx);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getErrors());
            assertFalse(response.getBody().getErrors().isEmpty());
            assertEquals(2, response.getBody().getErrors().size());

            boolean nameErrorFound = false;
            boolean priceErrorFound = false;

            for (Map<String, Object> error : response.getBody().getErrors()) {
                String detail = (String) error.get("detail");
                if (detail.contains("Name is required")) {
                    nameErrorFound = true;
                } else if (detail.contains("Price must be positive")) {
                    priceErrorFound = true;
                }
                assertEquals("400", error.get("status"));
                assertEquals("Validation Error", error.get("title"));
            }

            assertTrue(nameErrorFound, "Name validation error not found in response");
            assertTrue(priceErrorFound, "Price validation error not found in response");
        }
    }
}